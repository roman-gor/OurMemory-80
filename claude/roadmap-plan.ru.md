# Дальнейший план: QR-коды, экскурсии, свеча памяти, материалы от родственников

## Контекст

Уже сделано:
- экран деталей в одну ленту;
- модель `Burial` и полноэкранная карта с кластерами, «где я» и карточкой места;
- экран «О кладбище»;
- нижние вкладки «Ветераны / Карта / О кладбище»;
- edge-to-edge с плавающей iOS-панелью;
- заглушки фото.

Дальше по дорожной карте из `claude/details-redesign-and-burials-plan.ru.md` четыре фичи, которые связывают приложение с настоящим кладбищем и людьми:
1. QR-коды на могилах.
2. Экскурсии-маршруты.
3. Свеча памяти и «В этот день».
4. Материалы от родственников.

Перед ними — хвосты текущей итерации (шаг 0) и технический долг, который мешает масштабировать данные (шаг 5).

Рекомендуемый порядок: **0 → 1 → 5 → 2 → 3 → 4**.
- QR (1) даёт самую заметную связь «табличка → приложение» и почти не трогает существующий код.
- Кеш и тесты (5) нужны до экскурсий: экскурсии добавляют ещё один экран, который грузит всех ветеранов и все места.
- Материалы от родственников (4) — последние: им нужны авторизация, Storage и модерация.

## Шаг 0. Хвосты текущей итерации

### 0.1 Ошибка загрузки на главном экране

**Почему.** В `HomeViewModel` блок `catch` создаёт `HomeUiState.Error`, но не отправляет его в поток (нет `emit`). Если Firebase недоступен, главный экран навсегда остаётся на индикаторе загрузки, и экран ошибки не показывается.

```kotlin
}.catch { error ->
    emit(HomeUiState.Error(error))
}.stateIn(
    scope = viewModelScope,
    initialValue = HomeUiState.Loading,
    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS)
)
```

### 0.2 Gradle wrapper в репозитории

**Почему.** `.gitignore` исключает `*.jar`, поэтому `gradle/wrapper/gradle-wrapper.jar` не попадает в git, и `./gradlew` на чистом клоне падает с `ClassNotFoundException: GradleWrapperMain`.

```gitignore
*.jar
!gradle/wrapper/gradle-wrapper.jar
```

Сгенерировать jar закешированным дистрибутивом той же версии:

```bash
~/.gradle/wrapper/dists/gradle-9.7.1-bin/*/gradle-9.7.1/bin/gradle wrapper --gradle-version 9.7.1
git add gradle/wrapper/gradle-wrapper.jar .gitignore
```

### 0.3 Строки

**Почему.**
- `narodny_artist` в обоих языках содержит «Медаль "За отвагу"». У деятелей искусства показывается чужая награда.
- «Начать» на интро захардкожен в коде и не переводится.
- Названия наград в `values-be` не переведены.

```xml
<string name="narodny_artist">Народный артист СССР</string>
<string name="start">Начать</string>
```

```xml
<string name="start">Пачаць</string>
```

Точное название награды (СССР или БССР) сверить с данными ветеранов. `IntroScreen` берёт текст через `stringResource(R.string.start)`.

### 0.4 Проверка на устройстве того, что уже сделано

**Почему.** Сборка и Detekt проходят, но на телефоне не проверены:
- прокрутка деталей и «О кладбище» (панель становится сплошной, значки статус-бара меняют цвет);
- экран карты;
- метка «Я»;
- то, что интро после установки сразу сменилось «Ветеранами».

Сценарии — в разделе «Проверка», пункт 0.

## Шаг 1. QR-коды на могилах

### 1.1 Домен и связь домена с приложением (App Links)

**Почему.** QR должен работать у всех. Если приложение установлено, открывается карточка ветерана. Если нет — открывается веб-страница. Для этого QR содержит обычную https-ссылку на домен, который вы контролируете, а Android по файлу `assetlinks.json` на этом домене понимает, что ссылки можно открывать в приложении без диалога выбора. Firebase Hosting бесплатен и живёт в том же проекте `chatroom-85fb8`, поэтому домен — `chatroom-85fb8.web.app`.

`hosting/public/.well-known/assetlinks.json`:

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.gorman.ourmemoryapp",
      "sha256_cert_fingerprints": [
        "<SHA-256 release-ключа>",
        "<SHA-256 debug-ключа>"
      ]
    }
  }
]
```

Отпечатки: `keytool -list -v -keystore <keystore> -alias <alias>` (для debug — `~/.android/debug.keystore`, пароль `android`).

`hosting/firebase.json`:

```json
{
  "hosting": {
    "public": "public",
    "rewrites": [{ "source": "/veteran/**", "destination": "/veteran.html" }]
  }
}
```

### 1.2 Приём ссылки в приложении

**Почему.** Нужно, чтобы система отдавала ссылки `https://chatroom-85fb8.web.app/veteran/{id}` приложению, а навигация открывала по ним экран деталей.

`AndroidManifest.xml`, внутри `MainActivity`:

```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="https"
        android:host="chatroom-85fb8.web.app"
        android:pathPrefix="/veteran/" />
</intent-filter>
```

`AppNavigation.kt`:

```kotlin
composable(
    route = "${Screen.DetailScreen.route}/{veteranId}",
    deepLinks = listOf(navDeepLink { uriPattern = "$VETERAN_LINK_BASE/{veteranId}" })
) { ... }
```

```kotlin
private const val VETERAN_LINK_BASE = "https://chatroom-85fb8.web.app/veteran"
```

### 1.3 Правильный стек «назад» после сканирования

**Почему.** Для deep link Navigation строит стек от стартового экрана графа. Сейчас стартовый — интро, поэтому «назад» с карточки вернёт человека на интро, а не в список. Кроме того, если приложение уже открыто, новый QR придёт через `onNewIntent`, и его нужно передать в `NavController`.

```kotlin
@Composable
fun AppNavigation(openedFromLink: Boolean, onChangeLangClick: (String) -> Unit) {
    val navController = rememberNavController()
    val activity = LocalActivity.current as ComponentActivity

    DisposableEffect(navController, activity) {
        val listener = Consumer<Intent> { navController.handleDeepLink(it) }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = if (openedFromLink) Screen.HomeScreen.route else Screen.IntroScreen.route
    ) { ... }
}
```

В `MainActivity`: `AppNavigation(openedFromLink = intent?.data != null, ...)`, в манифесте у активити `android:launchMode="singleTop"`.

### 1.4 Страница для тех, у кого нет приложения

**Почему.** Посетитель без приложения должен увидеть хотя бы имя, годы и портрет, а также ссылку на установку. Иначе QR на табличке для него бесполезен. Данные берутся из той же базы через REST, дублировать их не нужно.

`hosting/public/veteran.html` (фрагмент):

```html
<main id="card"></main>
<script>
  const id = location.pathname.split("/").pop();
  fetch(`https://chatroom-85fb8-default-rtdb.firebaseio.com/Veterans.json`)
    .then(response => response.json())
    .then(veterans => {
      const veteran = Object.values(veterans).find(item => item.id === id);
      document.getElementById("card").innerHTML = veteran
        ? `<img src="${veteran.portrait}"><h1>${veteran.name}</h1><p>${veteran.years}</p><p>${veteran.baseInfo}</p>`
        : "<h1>Ветеран не найден</h1>";
    });
</script>
```

Ссылку на установку добавить, когда приложение появится в магазине. Работает только если правила Realtime Database разрешают публичное чтение `Veterans` — так же, как для приложения.

### 1.5 Лист QR для печати табличек

**Почему.** Печатать QR по одному вручную долго, и легко ошибиться с id. Скрипт берёт ветеранов и места из базы и собирает PDF: на каждой карточке QR, ФИО и «Участок · ряд · место», чтобы табличку повесили на нужную могилу.

`tools/qr/generate_qr_sheet.py`:

```python
import io
import requests
import qrcode
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib.utils import ImageReader
from reportlab.pdfgen import canvas

DATABASE_URL = "https://chatroom-85fb8-default-rtdb.firebaseio.com"
LINK_BASE = "https://chatroom-85fb8.web.app/veteran"
CARD_WIDTH = 60 * mm
CARD_HEIGHT = 80 * mm
QR_SIZE = 50 * mm


def load(node):
    return list((requests.get(f"{DATABASE_URL}/{node}.json").json() or {}).values())


def qr_image(url):
    buffer = io.BytesIO()
    qrcode.make(url, box_size=10, border=2).save(buffer, format="PNG")
    buffer.seek(0)
    return ImageReader(buffer)


def main():
    burials = {burial["id"]: burial for burial in load("Burials")}
    pdf = canvas.Canvas("qr_sheet.pdf", pagesize=A4)
    page_width, page_height = A4
    columns = int(page_width // CARD_WIDTH)
    rows = int(page_height // CARD_HEIGHT)
    for index, veteran in enumerate(load("Veterans")):
        slot = index % (columns * rows)
        if index and slot == 0:
            pdf.showPage()
        x = (slot % columns) * CARD_WIDTH
        y = page_height - (slot // columns + 1) * CARD_HEIGHT
        pdf.drawImage(qr_image(f"{LINK_BASE}/{veteran['id']}"), x + 5 * mm, y + 25 * mm, QR_SIZE, QR_SIZE)
        pdf.setFont("Helvetica", 9)
        pdf.drawString(x + 5 * mm, y + 18 * mm, veteran["name"])
        burial = burials.get(veteran.get("burialId", ""))
        if burial:
            pdf.drawString(x + 5 * mm, y + 12 * mm, f"Уч. {burial['section']} · ряд {burial['row']} · место {burial['place']}")
    pdf.save()


if __name__ == "__main__":
    main()
```

Для кириллицы в PDF зарегистрировать TTF-шрифт (например, Mulish из `app/src/main/res/font`) через `pdfmetrics.registerFont`: стандартная Helvetica кириллицу не рисует.

## Шаг 2. Экскурсии-маршруты

### 2.1 Модель данных

**Почему.** Экскурсия — это упорядоченный список мест с рассказом на каждой остановке. Места уже есть (`Burials`), поэтому экскурсия ссылается на них по `burialId` и не дублирует координаты.

```json
"Tours": {
  "t_heroes": {
    "id": "t_heroes",
    "title": "Герои Советского Союза",
    "description": "Маршрут на 40 минут по участкам 1–3",
    "stops": [
      { "burialId": "b_004", "text": "…", "audioUrl": "https://firebasestorage.googleapis.com/…" },
      { "burialId": "b_001", "text": "…", "audioUrl": "" }
    ]
  }
}
```

```kotlin
data class Tour(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val stops: List<TourStop> = emptyList()
)
```

```kotlin
data class TourStop(
    val burialId: String = "",
    val text: String = "",
    val audioUrl: String = ""
)
```

Репозиторий — по образцу `BurialsRepository`: `data/tours/datasource/remote/ToursRemoteDataSource(+Impl)`, `data/tours/repository/ToursRepositoryImpl`, `domain/repository/ToursRepository`, провайдеры в `AppModule`.

### 2.2 Потоковое аудио вместо `MediaPlayer` и raw-ресурсов

**Почему.**
- Аудиогид на остановках живёт в Firebase Storage, а `AudioRepository` умеет играть только `R.raw`.
- У всех ветеранов сейчас одна и та же запись `veteran_bio_10`.

Media3 ExoPlayer играет по URL, буферизует и корректно работает с плохой связью. Заодно у `Veteran` появляется своё поле `audioUrl`.

```toml
media3 = "1.8.0"
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
```

```kotlin
@Singleton
class AudioRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val player = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying, duration = duration.toInt())
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) _playbackState.value = AudioPlaybackState()
            }
        })
    }

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState = _playbackState.asStateFlow()

    fun playAudio(audioItem: AudioItem) {
        player.setMediaItem(MediaItem.fromUri(audioItem.url))
        player.prepare()
        player.play()
        _playbackState.value = AudioPlaybackState(isPlaying = true, currentAudio = audioItem)
    }
}
```

`AudioItem` получает поле `url` вместо `rawResourceId`. Версию Media3 сверить с актуальной на момент реализации. Воспроизведение с выключенным экраном во время экскурсии (`MediaSessionService`) — отдельным шагом после базовой версии.

### 2.3 Экран экскурсии

**Почему.** На месте человеку нужно видеть весь маршрут и номер следующей остановки, и слушать рассказ, не переключаясь между экранами. Поэтому карта с линией маршрута и пронумерованными метками, а снизу — список остановок с кнопкой воспроизведения.

Вход: чип «Экскурсии» в верхнем ряду карты открывает список экскурсий в `ModalBottomSheet`. Выбор экскурсии ведёт на `Screen.TourScreen` (`tour/{tourId}`).

```kotlin
val points = stops.map { Point(it.latitude, it.longitude) }
val route = map.mapObjects.addPolyline(Polyline(points)).apply {
    setStrokeColor(MemoryRed.toArgb())
    strokeWidth = ROUTE_STROKE_WIDTH
}
stops.forEachIndexed { index, stop ->
    map.mapObjects.addPlacemark().apply {
        geometry = Point(stop.latitude, stop.longitude)
        setIcon(NumberImageProvider(context, index + 1))
        userData = stop.burialId
        addTapListener(stopTapListener)
    }
}
```

`ClusterImageProvider` переименовывается в `NumberImageProvider`: он уже рисует бордовый круг с числом и подходит и для кластеров, и для номеров остановок. Линия сначала прямая между точками. Пешеходный маршрут по дорожкам (`PedestrianRouter` из navikit) — отдельным шагом, если прямые линии окажутся неудобными.

## Шаг 3. Свеча памяти и «В этот день»

### 3.1 Свеча памяти

**Почему.** Посетителям нужен простой способ почтить память — кнопка «Зажечь свечу» со счётчиком на карточке ветерана. Счётчик общий для всех, поэтому хранится в базе. Увеличивается транзакцией, чтобы одновременные нажатия не терялись. Одно нажатие в сутки с устройства — чтобы счётчик что-то значил.

```kotlin
class CandlesRemoteDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : CandlesRemoteDataSource {

    override fun observeCandles(veteranId: String): Flow<Long> = callbackFlow {
        val reference = database.getReference(CANDLES_PATH).child(veteranId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Long::class.java) ?: 0L)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    override suspend fun lightCandle(veteranId: String) = suspendCancellableCoroutine { continuation ->
        database.getReference(CANDLES_PATH).child(veteranId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.value = (currentData.getValue(Long::class.java) ?: 0L) + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) continuation.resumeWithException(error.toException()) else continuation.resume(Unit)
            }
        })
    }

    companion object {
        private const val CANDLES_PATH = "Candles"
    }
}
```

Дата последнего нажатия по ветерану хранится локально в DataStore Preferences (`androidx.datastore:datastore-preferences`), ключ `candle_lit_{veteranId}`. Правило базы разрешает только +1:

```json
"Candles": {
  "$veteranId": {
    ".read": true,
    ".write": true,
    ".validate": "newData.isNumber() && newData.val() === (data.exists() ? data.val() : 0) + 1"
  }
}
```

### 3.2 «В этот день»

**Почему.** Годовщины рождения и гибели — повод вспомнить конкретного человека, и главный экран может показывать их блоком над списком. Поле `years` — строка для отображения («1921 – 1985»), сравнивать по нему даты нельзя. Поэтому в базу добавляются `birthDate` и `deathDate` в формате `yyyy-MM-dd`.

```kotlin
data class Veteran(
    val birthDate: String = "",
    val deathDate: String = ""
)
```

```kotlin
private fun Veteran.isAnniversary(today: LocalDate) = listOf(birthDate, deathDate).any { date ->
    runCatching { LocalDate.parse(date) }.getOrNull()
        ?.let { it.month == today.month && it.dayOfMonth == today.dayOfMonth } == true
}
```

`HomeUiState.Success` получает `anniversaries: ImmutableList<Veteran>`, `HomeScreen` показывает горизонтальный ряд карточек «В этот день», когда список не пуст.

### 3.3 Напоминание к 9 Мая

**Почему.** Напоминание раз в год — повод открыть приложение и зажечь свечу. Сервер для этого не нужен: достаточно локального уведомления через WorkManager, которое после показа планирует себя на следующий год.

```kotlin
fun scheduleVictoryDayReminder(context: Context) {
    val now = ZonedDateTime.now()
    var next = now.withMonth(Month.MAY.value).withDayOfMonth(VICTORY_DAY).withHour(REMINDER_HOUR)
        .truncatedTo(ChronoUnit.HOURS)
    if (!next.isAfter(now)) next = next.plusYears(1)
    val request = OneTimeWorkRequestBuilder<VictoryDayReminderWorker>()
        .setInitialDelay(Duration.between(now, next))
        .build()
    WorkManager.getInstance(context)
        .enqueueUniqueWork(VICTORY_DAY_WORK, ExistingWorkPolicy.KEEP, request)
}
```

Worker показывает уведомление и вызывает `scheduleVictoryDayReminder` снова (с `ExistingWorkPolicy.REPLACE`). На Android 13+ нужно разрешение `POST_NOTIFICATIONS` — спрашивать его после первого открытия деталей, а не на старте.

## Шаг 4. Материалы от родственников

**Почему.** Главный источник новых фото, документов и воспоминаний — семьи ветеранов. Нужна форма «Дополнить историю» на карточке ветерана:
- текст;
- до 5 фото;
- контакт.

Данные попадают в отдельный узел с модерацией и никогда не публикуются автоматически. Чтобы форму нельзя было заспамить анонимно, пишет только пользователь с анонимной Firebase-авторизацией, а правила ограничивают размер и тип файлов.

Зависимости: `firebase-storage`, `firebase-auth` (из того же BOM).

```kotlin
data class Submission(
    val id: String = "",
    val veteranId: String = "",
    val text: String = "",
    val contact: String = "",
    val photoPaths: List<String> = emptyList(),
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L
)
```

```kotlin
val pickPhotos = rememberLauncherForActivityResult(
    ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS)
) { uris -> onUiIntent(SubmissionUiIntent.OnPhotosPicked(uris)) }
```

Отправка во ViewModel:

```kotlin
private suspend fun submit(form: SubmissionForm) {
    auth.signInAnonymously().await()
    val id = database.getReference(SUBMISSIONS_PATH).push().key ?: return
    val paths = form.photos.mapIndexed { index, uri ->
        val path = "$SUBMISSIONS_PATH/$id/$index.jpg"
        storage.getReference(path).putFile(uri).await()
        path
    }
    database.getReference(SUBMISSIONS_PATH).child(id).setValue(
        mapOf(
            "id" to id,
            "veteranId" to form.veteranId,
            "text" to form.text,
            "contact" to form.contact,
            "photoPaths" to paths,
            "status" to STATUS_PENDING,
            "createdAt" to ServerValue.TIMESTAMP
        )
    ).await()
}
```

Правила Realtime Database:

```json
"Submissions": {
  ".read": false,
  "$submissionId": {
    ".write": "auth != null && !data.exists()"
  }
}
```

Правила Storage:

```
match /Submissions/{submissionId}/{file} {
  allow read: if false;
  allow create: if request.auth != null
    && request.resource.size < 10 * 1024 * 1024
    && request.resource.contentType.matches('image/.*');
}
```

Модерация на первом этапе — вручную в Firebase Console: одобренное переносится в `veteransInfo` ветерана. В форме обязательна галочка согласия на обработку контакта: это персональные данные.

## Шаг 5. Технический долг перед масштабированием

### 5.1 Кеш ветеранов и мест

**Почему.**
- Главный экран, детали, карта, а дальше и экскурсии каждый раз загружают **всех** ветеранов и все места целиком.
- При переходе «карта → детали → назад» это два лишних запроса в Firebase.

Кеш в репозитории с `Mutex` отдаёт один загруженный список всем экранам.

```kotlin
@Singleton
class VeteransRepositoryImpl @Inject constructor(
    private val firebaseDB: FirebaseDB,
    private val apiService: YandexApiService
) : VeteransRepository {

    private val mutex = Mutex()
    private var cachedVeterans: List<Veteran>? = null

    override suspend fun getAllVeterans(): List<Veteran> = mutex.withLock {
        cachedVeterans ?: firebaseDB.getAllVeterans().also { cachedVeterans = it }
    }
}
```

Так же — `BurialsRepositoryImpl`. `@Singleton` у реализаций уже обеспечивает `AppModule`.

### 5.2 Главный экран и интро — в пакеты фич

**Почему.** По правилам `CLAUDE.md` код группируется по фичам. `ui/screens`, `ui/states` и `ui/viewModel` остались от старой структуры, и новые файлы туда класть нельзя.

```text
ui/home/
├── models/   HomeUiState.kt, HomeUiIntent.kt
├── ui/       HomeScreen.kt
└── viewmodels/HomeViewModel.kt
ui/intro/
└── ui/       IntroScreen.kt
```

### 5.3 Первые unit-тесты

**Почему.** Самая хрупкая логика — чистые преобразования данных. Если её сломать, на экране это заметно не сразу:
- группировка наград «×N»;
- разбор `veteransInfo` на абзацы и ссылки;
- правило видимости меток на карте.

Тестов в проекте нет вообще, начать стоит с них.

```toml
junit = { module = "junit:junit", version = "4.13.2" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
```

`app/src/test/java/com/gorman/ourmemoryapp/ui/map/viewmodels/MapViewModelTest.kt`:

```kotlin
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun monumentWithoutVeteransStaysVisibleWhenAllFiltersAreOff() = runTest {
        val viewModel = MapViewModel(
            savedStateHandle = SavedStateHandle(),
            burialsRepository = FakeBurialsRepository(listOf(monument, warGrave)),
            veteransRepository = FakeVeteransRepository(listOf(warVeteran))
        )
        viewModel.onUiIntent(MapUiIntent.OnCheckedWarChange(false))
        viewModel.onUiIntent(MapUiIntent.OnCheckedArtChange(false))

        val state = viewModel.uiState.first { it is MapUiState.Success } as MapUiState.Success

        assertEquals(listOf(monument.id), state.markers.map { it.id })
    }
}
```

`MainDispatcherRule` подменяет `Dispatchers.Main` на `StandardTestDispatcher`. `flowOn(Dispatchers.IO)` во ViewModel для тестов стоит заменить на внедряемый диспетчер (`@IoDispatcher CoroutineDispatcher` из `AppModule`).

## Проверка

**Общее для каждого шага:** `./gradlew assembleDebug`, `./gradlew detektAll`, с шага 5 — `./gradlew testDebugUnitTest`. Установка на устройство: `./gradlew installDebug`.

**0. Хвосты:**
- Выключить сеть, стереть данные приложения, открыть: на главном экране ошибка, а не вечный индикатор.
- `./gradlew --version` на чистом клоне работает.
- Прокрутить детали и «О кладбище»: панель становится сплошной, в центре появляется имя или название, значки статус-бара над фото белые, после прокрутки тёмные.
- Карта: метки, кластеры, карточка места, «где я» с бордовой точкой.
- Интро показывается при каждом холодном старте, «Начать» ведёт на «Ветеранов».
- Белорусский язык: «Пачаць», вкладки, награды.

**1. QR:**
- `firebase deploy --only hosting`, затем открыть `https://chatroom-85fb8.web.app/.well-known/assetlinks.json` в браузере.
- `adb shell pm verify-app-links --re-verify com.gorman.ourmemoryapp`, затем `adb shell pm get-app-links com.gorman.ourmemoryapp`: домен в состоянии `verified`.
- `adb shell am start -a android.intent.action.VIEW -d "https://chatroom-85fb8.web.app/veteran/10"`: открываются детали без диалога выбора, «назад» ведёт в список, а не на интро.
- Та же команда при уже открытом приложении на другой вкладке: открываются детали.
- Отсканировать QR из `qr_sheet.pdf` камерой телефона без приложения: веб-страница с именем, годами и портретом.

**2. Экскурсии:**
- Карта → «Экскурсии» → выбрать: линия маршрута и метки 1…N по порядку.
- Тап по остановке: текст и аудио.
- Аудио стартует по URL при медленной сети (ограничить в настройках разработчика), доигрывает до конца, кнопка возвращается в «play».

**3. Свеча и «В этот день»:**
- Зажечь свечу на двух устройствах одновременно: счётчик +2.
- Повторное нажатие в тот же день недоступно.
- Запись в `Candles` со значением +5 из REST отклоняется правилами.
- Поставить ветерану `birthDate` на сегодняшнюю дату: на главном блок «В этот день».
- Выставить на устройстве дату 8 мая 23:59: уведомление 9 мая, следующая задача в WorkManager запланирована на следующий год (`adb shell dumpsys jobscheduler | grep ourmemory`).

**4. Материалы от родственников:**
- Отправить форму с 3 фото: запись `Submissions/{id}` со статусом `pending`, файлы в Storage.
- Повторная запись в тот же `id` отклоняется.
- Файл больше 10 МБ или не картинка отклоняется.
- Без галочки согласия кнопка отправки недоступна.

**5. Тех. долг:**
- Переход «Карта → детали → назад → детали другого ветерана»: в логах Firebase один запрос `Veterans`.
- Тесты зелёные, в том числе после намеренной поломки правила видимости: тест падает.
