# Редизайн экрана деталей и модель данных для карты кладбища

## Контекст

Экран деталей ветерана (`ui/screens/DetailsScreen.kt`, 566 строк) перегружен и местами сломан:

- Три страницы `HorizontalPager` без видимых вкладок. Подсказку «свайпни влево» даёт Lottie-анимация, которая занимает примерно 40% первой страницы (`weight(2.3f)`).
- Текст скроллится внутри карточек с фиксированной высотой: `verticalScroll` внутри `Card` с `weight(3.7f)`. На маленьком экране это скролл в скролле.
- Баги:
  - `BioContent` показывает только `infoText.first()`, остальные абзацы биографии теряются.
  - `DocContent` проверяет `infoRes.isNotEmpty()`, а листает `directedUrls`.
  - Кнопка play/pause хранит своё `isPlaying` в `remember` и не смотрит на `playbackState`. Когда трек заканчивается, кнопка продолжает показывать «пауза». Перемотки нет, хотя `AudioAction.SeekTo` уже есть.
  - Награда с ID 3 (Суворов II степени) показывает строку и картинку I степени.
- Сопоставление «ID награды → картинка» продублировано: оно есть и в `getRewardResId()`, и в `RewardsDisplay`.
- В UI захардкожены цвета и строки: `"Error occurred"`, `"Play"`, `"Биография ветерана"`.

Дальше планируется интерактивная карта кладбища с метками ветеранов (координаты, фото, информация). В этой итерации:

1. Экран деталей становится одной лентой: шапка с портретом, награды, аудиоплеер, биография, галерея, место захоронения.
2. Вводится модель данных «место захоронения» (`Burial`) в Firebase и в коде. Экран деталей её уже показывает: номер участка, ряда и места и мини-карту.

Сам экран карты с метками, а также QR-коды, экскурсии, свеча памяти и материалы от родственников идут в следующие итерации (см. «Дорожная карта»).

Первое действие после одобрения — сохранить этот план в `claude/details-redesign-and-burials-plan.ru.md`.

## Итерация 1

### 1. Модель места захоронения в Firebase

**Почему.** Метка на карте — это не ветеран, а место:
- в братской могиле лежат несколько человек;
- у памятника или мемориала ветерана нет вовсе.

Поэтому место хранится отдельным узлом `Burials`, а ветеран ссылается на него через `burialId`. Номер участка, ряда и места обязателен: погрешность GPS 5–10 м, а могилы стоят плотно, так что по одной метке могилу не найти.

```json
"Burials": {
  "b_017": {
    "id": "b_017",
    "latitude": 53.90912,
    "longitude": 27.58705,
    "section": "3",
    "row": "5",
    "place": "12",
    "type": "GRAVE",
    "photo": "https://disk.yandex.ru/i/...",
    "description": ""
  }
},
"Veterans": {
  "10": { "...": "...", "burialId": "b_017" }
}
```

`type`: `GRAVE`, `MASS_GRAVE` или `MONUMENT`. В домене (`domain/model/`, каждый тип в своём файле):

```kotlin
data class Burial(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val section: String = "",
    val row: String = "",
    val place: String = "",
    val type: String = "",
    val photo: String = "",
    val description: String = ""
)
```

```kotlin
data class Veteran(
    val id: String = "",
    val name: String = "",
    val portrait: String = "",
    val baseInfo: String = "",
    val allInfo: String = "",
    val years: String = "",
    val category: String = "",
    val rewards: String = "",
    val veteransInfo: List<String> = emptyList(),
    val burialId: String = ""
)
```

`Screen` переезжает из `VeteransModel.kt` в собственный файл (правило «один тип на файл»).

### 2. Источник данных и репозиторий мест

**Почему.** Карте понадобятся все места, экрану деталей — одно место по `burialId`. Отдельный репозиторий не раздувает `VeteransRepository`. Кроме того, на кладбище плохая связь, поэтому включаем офлайн-кеш Firebase: один раз загруженные данные будут открываться и без сети.

```text
data/burials/
├── datasource/remote/
│   ├── BurialsRemoteDataSource.kt
│   └── BurialsRemoteDataSourceImpl.kt
└── repository/
    └── BurialsRepositoryImpl.kt
domain/repository/BurialsRepository.kt
```

```kotlin
class BurialsRemoteDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : BurialsRemoteDataSource {

    override suspend fun getAllBurials(): List<Burial> {
        val snapshot = database.getReference(BURIALS_PATH).get().await()
        return snapshot.children.mapNotNull { it.getValue(Burial::class.java) }
    }

    companion object {
        private const val BURIALS_PATH = "Burials"
    }
}
```

`await()` берётся из уже подключённого `kotlinx-coroutines-play-services`. В `AppModule`:

```kotlin
@Provides
@Singleton
fun provideFirebaseDatabase(): FirebaseDatabase {
    return FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) }
}

@Provides
@Singleton
fun provideBurialsRepository(dataSource: BurialsRemoteDataSource): BurialsRepository =
    BurialsRepositoryImpl(dataSource)
```

### 3. Перенос экрана деталей в пакет фичи и новое UI-состояние

**Почему.** Сейчас в `DetailsUiState.Success` лежат три пересекающиеся коллекции: `additionalInfo`, `additionalRes` и `directUrls`. Экрану нужны готовые модели:
- абзацы биографии;
- медиа с подписями;
- награды с картинкой и названием;
- место захоронения.

Новые файлы кладутся по правилам пакетов: `ui/details/{models,ui,viewmodels}`. Сюда же переезжают `DetailsViewModel`, `DetailsUiEvent`, `AudioAction`.

```kotlin
sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(
        val veteran: Veteran,
        val rewards: ImmutableList<Reward>,
        val paragraphs: ImmutableList<String>,
        val media: ImmutableList<MediaUi>,
        val audio: AudioItem?,
        val burial: BurialUi?
    ) : DetailsUiState
    data object Error : DetailsUiState
}
```

```kotlin
data class MediaUi(
    val url: String,
    val description: String
)
```

```kotlin
data class BurialUi(
    val latitude: Double,
    val longitude: Double,
    val section: String,
    val row: String,
    val place: String
)
```

Награды становятся enum, и сопоставление перестаёт дублироваться (попутно исправлен ID 3 → `suvorov_2`):

```kotlin
enum class Reward(val id: Int, @DrawableRes val iconRes: Int, @StringRes val nameRes: Int) {
    RED_BANNER(1, R.drawable.red_znamya, R.string.red_znamya),
    SUVOROV_FIRST(2, R.drawable.suvorov_1, R.string.suvorov_1),
    SUVOROV_SECOND(3, R.drawable.suvorov_1, R.string.suvorov_2),
    LENIN(4, R.drawable.lenin, R.string.lenin),
    HERO_USSR(5, R.drawable.geroj_sssr, R.string.geroj_sssr),
    PARTISAN(6, R.drawable.partizan, R.string.partizan),
    VICTORY_OVER_GERMANY(7, R.drawable.za_pobedu_germany, R.string.za_pobedu_germany),
    PATRIOTIC_WAR(8, R.drawable.otech_war, R.string.otech_war),
    RED_STAR(9, R.drawable.red_star, R.string.red_star),
    BADGE_OF_HONOUR(10, R.drawable.orden_znak_pocheta, R.string.orden_znak_pocheta),
    FOR_COURAGE(11, R.drawable.za_otvagu, R.string.za_otvagu),
    PEOPLES_ARTIST(12, R.drawable.narodny_artist, R.string.narodny_artist);

    companion object {
        fun fromId(id: Int): Reward? = entries.firstOrNull { it.id == id }
    }
}
```

`DetailsViewModel` строит состояние в `observeDetailsUiState()`: ветеран и место грузятся параллельно, `veteransInfo` раскладывается на абзацы и медиа.

```kotlin
private fun observeDetailsUiState(): Flow<DetailsUiState> = flow {
    val veteran = veteranRepository.getAllVeterans().first { it.id == veteranId }
    val burial = burialsRepository.getAllBurials().firstOrNull { it.id == veteran.burialId }
    val (links, paragraphs) = veteran.veteransInfo.partition { it.contains(LINK_MARKER) }
    emit(
        DetailsUiState.Success(
            veteran = veteran,
            rewards = veteran.rewards.split(REWARDS_SEPARATOR)
                .mapNotNull { it.trim().toIntOrNull()?.let(Reward::fromId) }
                .toPersistentList(),
            paragraphs = paragraphs.toPersistentList(),
            media = resolveMedia(links).toPersistentList(),
            audio = loadAudioForVeteran(veteranId),
            burial = burial?.toExternalModel()
        )
    )
}
```

`resolveMedia` переиспользует нынешнюю логику `loadDirectedUrlSequentially`: ссылки Яндекс.Диска превращаются в прямые, при ошибке остаётся исходная. Возвращает `List<MediaUi>` и сохраняет порядок.

### 4. Аудиоплеер, связанный с реальным состоянием

**Почему.** Сейчас кнопка врёт после окончания трека, а прогресса нет. `AudioRepository.currentPosition` обновляется только на паузе. Нужен поток позиции, который тикает, пока идёт воспроизведение. Кроме того, звук должен останавливаться при уходе с экрана.

```kotlin
class AudioRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun observePosition(): Flow<Int> = flow {
        while (true) {
            emit(mediaPlayer?.currentPosition ?: 0)
            delay(POSITION_UPDATE_MILLIS)
        }
    }

    companion object {
        private const val POSITION_UPDATE_MILLIS = 500L
    }
}
```

```kotlin
val playbackState = audioRepository.playbackState
    .flatMapLatest { state ->
        if (state.isPlaying) {
            audioRepository.observePosition().map { state.copy(currentPosition = it) }
        } else {
            flowOf(state)
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AudioPlaybackState())

override fun onCleared() {
    audioRepository.stopAudio()
}
```

Экран берёт `isPlaying`, `currentPosition` и `duration` только из `playbackState`. Локальный `remember { isPlaying }` удаляется. `Slider` отправляет `AudioAction.SeekTo`.

### 5. Экран — одна лента

**Почему.** Вертикальная лента без вложенных скроллов читается естественно, пейджер и Lottie-подсказка не нужны, и всё содержимое видно. Порядок секций идёт от главного к второстепенному: кто это → награды → голос → история → документы → где лежит.

```text
ui/details/ui/
├── DetailsScreen.kt          Scaffold + TopAppBar(←) + LazyColumn
├── DetailsHeader.kt          портрет на всю ширину, ФИО, годы
├── RewardsRow.kt             LazyRow медалей → ModalBottomSheet с названием
├── AudioPlayerCard.kt        play/pause, Slider, время
├── BiographySection.kt       все абзацы; свёрнуто до N строк + «Читать полностью»
├── MediaGallery.kt           LazyRow превью
├── PhotoViewerDialog.kt      полноэкранный HorizontalPager + зум + подпись
└── BurialSection.kt          «Участок 3 · ряд 5 · место 12» + мини-карта
```

```kotlin
@Composable
fun DetailsScreen(
    detailsViewModel: DetailsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by detailsViewModel.playbackState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { DetailsTopBar(onBackClick = onBackClick) }
    ) { padding ->
        when (val state = uiState) {
            DetailsUiState.Loading -> LoadingContent(Modifier.padding(padding))
            DetailsUiState.Error -> ErrorContent(Modifier.padding(padding))
            is DetailsUiState.Success -> LazyColumn(contentPadding = padding) {
                item { DetailsHeader(state.veteran) }
                if (state.rewards.isNotEmpty()) item { RewardsRow(state.rewards) }
                state.audio?.let { item { AudioPlayerCard(it, playbackState, detailsViewModel::onUiEvent) } }
                if (state.paragraphs.isNotEmpty()) item { BiographySection(state.paragraphs) }
                if (state.media.isNotEmpty()) item { MediaGallery(state.media) }
                state.burial?.let { item { BurialSection(it) } }
            }
        }
    }
}
```

Мини-карта в `BurialSection` построена на `YandexMapView` из `InfoScreen.kt`, но её надо вынести в общий компонент с параметром `Point`. Сейчас `move()` и `addPlacemark()` вызываются в теле composable, то есть новая метка добавляется при каждой рекомпозиции. В общем компоненте они переносятся в `remember(point)`. Кнопку «Показать на карте» добавим, когда появится экран карты.

Стиль:
- цвета только через `MaterialTheme.colorScheme`;
- в `Theme.kt` отключается `dynamicColor`, чтобы фиолетовые цвета Material You не перебивали бордовую палитру;
- `primary` = `dark_red`, светлый фон;
- портрет с лёгким затемнением снизу, чтобы ФИО читалось поверх фото.

В `AppNavigation` передаётся `onBackClick = navController::popBackStack`. `SwipeAnimation` и зависимость Lottie для этого экрана больше не нужны (`swipe_left.json` удалить, если он нигде больше не используется).

### 6. Строки

**Почему.** По правилам проекта все надписи берутся из ресурсов, в `values/` и `values-be/` одновременно. Новые ключи повторяют текст строки:

```xml
<string name="listen_to_biography">Послушать биографию</string>
<string name="read_more">Читать полностью</string>
<string name="photos_and_documents">Фото и документы</string>
<string name="burial_place">Место захоронения</string>
<string name="section_row_place_msg">Участок %1$s · ряд %2$s · место %3$s</string>
<string name="failed_to_load_data_msg">Не удалось загрузить данные. Проверьте подключение к интернету</string>
<string name="play">Воспроизвести</string>
<string name="pause">Пауза</string>
```

## Дорожная карта (следующие итерации)

1. **Экран карты.** `MapScreen` на MapKit:
   - все `Burials` как метки с кластеризацией (`ClusterizedPlacemarkCollection`), спутниковый слой;
   - кнопка «где я»;
   - фильтры War/Art, как на главном экране;
   - тап по метке → bottom sheet со списком ветеранов этого места (фильтр по `burialId`) → экран деталей;
   - вход с главного экрана и через «Показать на карте» из деталей (камера центрируется на метке).
2. **QR-коды на могилах.**
   - Deep link `ourmemory://veteran/{id}` плюс https App Link, чтобы QR работал и без установленного приложения (открывается веб-страница или магазин).
   - Скрипт генерации QR для печати табличек.
3. **Экскурсии-маршруты.**
   - Узел `Tours`: название, описание, упорядоченный список `burialId`.
   - На карте — линия маршрута и нумерованные метки.
   - На каждой точке — аудиогид. Для потокового аудио из Storage `MediaPlayer` стоит заменить на Media3/ExoPlayer, заодно решается «одно аудио для всех».
4. **Свеча памяти и «В этот день».**
   - Счётчик `candles/{veteranId}` через `runTransaction`, одно нажатие в сутки с устройства.
   - Лента годовщин на главном экране по датам рождения и смерти. Для неё поле `years` придётся разложить на `birthDate` и `deathDate`.
   - Push к 9 Мая через FCM.
5. **Материалы от родственников.**
   - Форма: фото или документ, текст, контакт.
   - Firebase Storage и узел `Submissions` со статусом модерации.
   - Правила безопасности: запись только в `Submissions`, чтение только админам.
6. **Ещё идеи на обсуждение:**
   - ссылки «Найти в архивах» («Память народа», ОБД «Мемориал»);
   - справочник наград с фильтром ветеранов по награде;
   - поделиться карточкой ветерана картинкой;
   - тёмная тема;
   - крупный шрифт и TalkBack для пожилой аудитории;
   - викторина для школьников.

## Критичные файлы

- `app/src/main/java/com/gorman/ourmemoryapp/ui/screens/DetailsScreen.kt` → разбивается на `ui/details/ui/*`
- `ui/viewModel/DetailsViewModel.kt`, `ui/states/Details*.kt` → `ui/details/{viewmodels,models}/`
- `data/repository/AudioRepository.kt` — поток позиции
- `di/AppModule.kt` — persistence и `BurialsRepository`
- `domain/models/VeteransModel.kt` — `burialId`, вынос `Screen`
- `ui/screens/InfoScreen.kt` — `YandexMapView` выносится в общий компонент
- `ui/theme/Theme.kt`, `res/values*/strings.xml`
- `ui/AppNavigation.kt` — `onBackClick`

## Проверка

1. `./gradlew assembleDebug` и `./gradlew detektAll` проходят без новых замечаний.
2. В Firebase Console добавить узел `Burials` с 1–2 записями и проставить `burialId` двум ветеранам. Одному `burialId` не ставить.
3. Ручные сценарии на устройстве:
   - Ветеран с местом: видны портрет, награды, аудио, все абзацы биографии, галерея, «Участок · ряд · место» и мини-карта с одной меткой. При скролле не создаются дубликаты меток.
   - Ветеран без места: секции места нет, остальное на месте.
   - Тап по медали: bottom sheet с правильным названием. Для ID 3 — «II степени».
   - Аудио: play → ползунок движется → seek → трек доигрывает до конца → кнопка возвращается в «play». Уход назад во время воспроизведения останавливает звук.
   - Тап по превью фото: полноэкранный просмотр, листание, зум, подпись.
   - Кнопка «назад» в TopAppBar работает.
   - Режим полёта после первого открытия: данные открываются из кеша. Первый запуск без сети показывает экран ошибки из ресурсов.
   - Переключение на белорусский: все новые надписи переведены.
