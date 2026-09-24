# Иконки навигации, метка «Я» и редизайн экрана общей информации

## Контекст

После экрана карты на устройстве видны три проблемы:

1. **Стрелка «назад»** (`arrow_back.xml`) — крупная угловая скобка `arrow_back_ios` из Material Symbols. Выбрана стандартная стрелка Material с древком.
2. **Метка «Я»** на карте — стандартный значок Яндекса, он не в стиле приложения. Выбрана бордовая точка в белой обводке и полупрозрачный круг точности GPS.
3. **Экран общей информации** (`ui/screens/InfoScreen.kt`) остался в старом стиле:
   - текст истории скроллится внутри карточки фиксированной высоты 400dp;
   - тап по фото галереи ничего не делает (`// TODO onClick event`);
   - новости — синие ссылки, нажимается только текст;
   - строки «Back», «Monument», «БЕЛ»/«РУС» захардкожены;
   - карта прячется на `ON_STOP` через `isVisible` и после возврата в приложение больше не появляется.

   Экран переделывается в одну ленту, как детали ветерана. Добавляются:
   - кнопки «Карта кладбища» и «Проложить маршрут»;
   - блок «Часы работы и контакты». Данные пришлёте позже, поэтому блок скрыт, пока строки пустые. Выдумывать данные не буду.

Попутно найдена ошибка: `keyboard_arrow_right.xml` на самом деле рисует шеврон **влево**. `IntroScreen` компенсирует это `rotate(180f)`, а в `BurialBottomSheet` на карте стрелка смотрит не в ту сторону.

Первое действие после одобрения — сохранить план в `claude/info-screen-and-icons-plan.ru.md`.

## 1. Иконки стрелок

**Почему.** Стрелка с древком — стандарт Android для «назад», пользователи узнают её сразу. Правый шеврон должен быть правым, чтобы не приходилось поворачивать его в коде.

`res/drawable/arrow_back.xml` (Material Icons `arrow_back`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:autoMirrored="true">
  <path
      android:fillColor="#FF000000"
      android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z" />
</vector>
```

`res/drawable/keyboard_arrow_right.xml` (Material Symbols `chevron_right`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="960"
    android:viewportHeight="960"
    android:autoMirrored="true">
  <path
      android:fillColor="#FF000000"
      android:pathData="M504,480 L320,296l56,-56 240,240 -240,240 -56,-56 184,-184Z" />
</vector>
```

В `IntroScreen.kt` убирается `Modifier.rotate(180f)`. Цвет везде задаёт `Icon.tint`, поэтому `fillColor` в XML ни на что не влияет.

## 2. Метка «Я» на карте

**Почему.** Метка пользователя должна отличаться от меток могил и показывать точность GPS: на плотном кладбище погрешность 5–10 м важна. MapKit даёт менять значки через `UserLocationObjectListener`. Слушатель хранится по слабой ссылке, поэтому живёт в `remember`, как остальные слушатели на карте.

`ui/map/ui/UserLocationImageProvider.kt` рисует точку на `Canvas`, так же как `ClusterImageProvider`:

```kotlin
class UserLocationImageProvider(private val context: Context) : ImageProvider() {

    override fun getId() = ID

    override fun getImage(): Bitmap {
        val density = context.resources.displayMetrics.density
        val outerRadius = OUTER_RADIUS_DP * density
        val size = (outerRadius * 2).toInt()
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MemoryPaper.toArgb() }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MemoryRed.toArgb() }
        canvas.drawCircle(outerRadius, outerRadius, outerRadius, ringPaint)
        canvas.drawCircle(outerRadius, outerRadius, INNER_RADIUS_DP * density, dotPaint)
        return bitmap
    }

    companion object {
        private const val ID = "user_location_dot"
        private const val OUTER_RADIUS_DP = 11f
        private const val INNER_RADIUS_DP = 7.5f
    }
}
```

В `rememberMyLocationAction`:

```kotlin
val userLocationObjectListener = remember {
    object : UserLocationObjectListener {
        override fun onObjectAdded(view: UserLocationView) {
            val icon = UserLocationImageProvider(context)
            view.pin.setIcon(icon)
            view.arrow.setIcon(icon)
            view.accuracyCircle.fillColor = MemoryRed.copy(alpha = ACCURACY_FILL_ALPHA).toArgb()
            view.accuracyCircle.strokeColor = MemoryRed.copy(alpha = ACCURACY_STROKE_ALPHA).toArgb()
            view.accuracyCircle.strokeWidth = ACCURACY_STROKE_WIDTH
        }

        override fun onObjectRemoved(view: UserLocationView) = Unit

        override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) = Unit
    }
}

LaunchedEffect(userLocationLayer) {
    userLocationLayer.setObjectListener(userLocationObjectListener)
    if (context.hasLocationPermission()) userLocationLayer.isVisible = true
}
```

## 3. Жизненный цикл `MapView` по жизненному циклу экрана

**Почему.** Сейчас `rememberMapViewWithLifecycle` останавливает карту только при уходе composable из композиции. В фоне MapKit продолжает работать. `InfoScreen` обходит это флагом `isVisible`, который прячет карту на `ON_STOP` навсегда. Если слушать `ON_START`/`ON_STOP` владельца жизненного цикла, это чинит все три карты (детали, инфо, полноэкранная), и хак становится не нужен.

```kotlin
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.start()
                Lifecycle.Event.ON_STOP -> mapView.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.stop()
        }
    }
    return mapView
}

private fun MapView.start() {
    onStart()
    MapKitFactory.getInstance().onStart()
}

private fun MapView.stop() {
    onStop()
    MapKitFactory.getInstance().onStop()
}
```

`addObserver` сразу отправляет `ON_START`, если экран уже запущен, поэтому отдельный стартовый вызов не нужен.

## 4. Общие компоненты — в `ui/common`

**Почему.** Экран информации использует те же блоки, что и детали:
- заголовок секции;
- сворачиваемый текст;
- галерея с полноэкранным просмотром.

Если оставить их в `ui/details`, фича информации будет зависеть от фичи деталей. `BiographySection` превращается в универсальную `ExpandableTextSection` с параметром `title`, `MediaGallery` тоже получает `title`.

```text
ui/common/
├── models/MediaUi.kt                   ← ui/details/models
└── ui/
    ├── SectionTitle.kt                 ← ui/details/ui
    ├── ExpandableTextSection.kt        ← ui/details/ui/BiographySection.kt + title
    ├── MediaGallery.kt                 ← ui/details/ui + title
    ├── PhotoViewerDialog.kt            ← ui/details/ui
    └── BackTopBar.kt                   + параметр actions
```

```kotlin
@Composable
fun ExpandableTextSection(
    title: String,
    paragraphs: ImmutableList<String>,
    modifier: Modifier = Modifier
)
```

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    title: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
)
```

В деталях: `ExpandableTextSection(title = stringResource(R.string.biography), ...)` и `MediaGallery(title = stringResource(R.string.docs), ...)`.

## 5. Экран информации — одна лента

**Почему.**
- Длинная история в карточке со своим скроллом неудобна.
- Галерея не открывается.
- Новости выглядят как голые ссылки.

Лента повторяет экран деталей, поэтому приложение выглядит единообразно. Порядок секций: что это → история → как выглядит → где находится и как добраться → контакты → что о нём пишут.

```text
ui/info/
├── models/
│   ├── NewsUi.kt
│   └── InfoContent.kt         object: news, galleryImages (перенос из domain/models/Images.kt)
└── ui/
    ├── InfoScreen.kt          Scaffold + BackTopBar(действие «БЕЛ»/«РУС») + LazyColumn
    ├── InfoHero.kt            warwar.jpg, затемнение, «Военное кладбище», адрес
    ├── LocationSection.kt     адрес, MapPreview, «Карта кладбища», «Проложить маршрут»
    ├── ContactsSection.kt     часы работы, телефон (скрыт, пока строки пустые)
    └── NewsSection.kt         карточки источников, тап по всей карточке открывает статью
```

```kotlin
data class NewsUi(
    @param:DrawableRes val iconRes: Int,
    val title: String,
    val url: String
)
```

```kotlin
@Composable
fun InfoScreen(
    onBackClick: () -> Unit,
    onOpenMapClick: () -> Unit,
    onChangeLangClick: (String) -> Unit
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val isRussian = locale.language == RUSSIAN_LANGUAGE
    val history = stringResource(R.string.information)
    val paragraphs = remember(history) {
        history.split(PARAGRAPH_SEPARATOR).map { it.trim() }.filter { it.isNotBlank() }.toPersistentList()
    }
    val gallery = remember(context) {
        InfoContent.galleryImages.map { MediaUi(url = context.drawableUri(it), description = "") }.toPersistentList()
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = stringResource(R.string.warHeader),
                onBackClick = onBackClick,
                actions = {
                    TextButton(onClick = { onChangeLangClick(if (isRussian) BELARUSIAN_LANGUAGE else RUSSIAN_LANGUAGE) }) {
                        Text(stringResource(if (isRussian) R.string.bel else R.string.rus))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, verticalArrangement = Arrangement.spacedBy(28.dp)) {
            item { InfoHero() }
            item { ExpandableTextSection(title = stringResource(R.string.historyInfoHeader), paragraphs = paragraphs) }
            item { MediaGallery(title = stringResource(R.string.galleryHeader), media = gallery) }
            item { LocationSection(onOpenMapClick = onOpenMapClick) }
            item { ContactsSection() }
            item { NewsSection(news = InfoContent.news) }
        }
    }
}
```

Фото галереи — ресурсы приложения. Coil понимает URI `android.resource://<package>/<id>`, поэтому `PhotoViewerDialog` и `MediaUi(url: String)` переиспользуются без изменений.

«Проложить маршрут» открывает Яндекс Карты (приложение, а если его нет — браузер) с маршрутом до входа:

```kotlin
private fun Context.openRouteToCemetery() {
    val uri = "https://yandex.ru/maps/?rtext=~${CemeteryLocation.LATITUDE},${CemeteryLocation.LONGITUDE}".toUri()
    startActivity(Intent(Intent.ACTION_VIEW, uri))
}
```

`ContactsSection` показывает строку только если она не пустая. Если пустые обе, секции нет вовсе. Тап по телефону открывает набор номера (`Intent.ACTION_DIAL`).

Навигация: `InfoScreen` из `ui/screens` переезжает в `ui/info/ui`. Параметр `navigateToImage` удаляется: галерея открывается внутри экрана. Добавляется `onOpenMapClick = { navController.navigate(Screen.MapScreen.route) }`. `domain/models/Images.kt` удаляется: данные экрана переезжают в `ui/info/models/InfoContent.kt`.

## 6. Строки

```xml
<string name="bel" translatable="false">БЕЛ</string>
<string name="rus" translatable="false">РУС</string>
<string name="get_directions">Проложить маршрут</string>
<string name="opening_hours">Часы работы</string>
<string name="phone">Телефон</string>
<string name="cemetery_opening_hours"></string>
<string name="cemetery_phone" translatable="false"></string>
```

Строки `cemetery_opening_hours` и `cemetery_phone` остаются пустыми, пока вы не пришлёте данные. Когда они заполнены, блок появляется сам. Ключи названы по смыслу, а не по тексту: текста ещё нет. Белорусские переводы — для `get_directions`, `opening_hours`, `phone`, `cemetery_opening_hours`.

## Критичные файлы

- `res/drawable/arrow_back.xml`, `res/drawable/keyboard_arrow_right.xml`, `ui/screens/IntroScreen.kt`
- `ui/map/ui/MyLocationAction.kt`, новый `ui/map/ui/UserLocationImageProvider.kt`
- `ui/common/ui/MapViewLifecycle.kt`
- перенос в `ui/common`: `SectionTitle`, `BiographySection` → `ExpandableTextSection`, `MediaGallery`, `PhotoViewerDialog`, `MediaUi`; правки `ui/details/ui/DetailsScreen.kt`, `BackTopBar.kt`
- `ui/screens/InfoScreen.kt` → `ui/info/ui/*`; `ui/info/models/*`; удаление `domain/models/Images.kt`
- `ui/AppNavigation.kt`, `res/values*/strings.xml`

## Проверка

1. `./gradlew assembleDebug` и `./gradlew detektAll` проходят.
2. На подключённом устройстве (`adb install -r`, скриншоты через `adb exec-out screencap`):
   - Стрелка «назад» в деталях, на карте, на экране информации и в просмотре фото — стрелка с древком. На интро кнопка «Начать» показывает шеврон вправо. В карточке места на карте шеврон тоже смотрит вправо.
   - Карта → «где я» (разрешение можно выдать через `adb shell pm grant com.gorman.ourmemoryapp android.permission.ACCESS_FINE_LOCATION`): видна бордовая точка в белой обводке и бордовый полупрозрачный круг точности.
   - Экран информации:
     - шапка с фото и адресом;
     - история свёрнута, «Развернуть» раскрывает её полностью;
     - тап по фото галереи открывает полноэкранный просмотр с листанием и зумом;
     - мини-карта с меткой, «Карта кладбища» открывает экран карты, «Проложить маршрут» открывает Яндекс Карты или браузер с маршрутом;
     - блока контактов нет (строки пустые);
     - тап по карточке новости открывает статью;
     - «БЕЛ» переключает язык, после пересоздания экрана все надписи на белорусском.
   - Свернуть приложение на экране информации и вернуться: мини-карта на месте (раньше пропадала).
