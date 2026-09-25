# Многомодульность, стартовый экран и фиксация текущих правок

## Контекст

Сейчас весь код в одном модуле `:app`: 96 файлов импортируют общий `R`, фичи свободно ссылаются друг на друга, а правило «UI не лезет в data» держится только на дисциплине. Пользователь хочет архитектуру по принципу своего проекта Local-Events-Map: `build-logic` с convention-плагинами, модули `:core:*` и `:feature:*`, фичи не зависят друг от друга, у каждого модуля свои строки.

Решения, принятые с пользователем:
- **Навигация** остаётся на Navigation Compose. Маршруты (`Screen.kt`) переезжают из `domain` в `:core:navigation`, графы собирает `:app`. Navigation3 и деление на `api`/`impl` не берём: экраны и так навигируют через лямбды `onXClick`, поэтому чужие api-модули фичам не нужны.
- **Админка** — один модуль `:feature:admin` с текущими подпакетами.
- **Строки и ресурсы** разносятся по модулям. Всё, что нужно двум модулям и больше, уходит в `:core:ui`.
- **Стартовый экран** показывается только при первом запуске и получает новый дизайн.

Что берём из Local-Events-Map и что делаем лучше:
- **Берём:** `includeBuild("build-logic")` с общим каталогом версий, мелкие convention-плагины, `nonTransitiveRClass`, один корневой `detektAll`, Hilt-модули внутри своих модулей.
- **Делаем лучше:** domain — чистый Kotlin/JVM-модуль с интерфейсами репозиториев; есть `:core:testing` с фейками; `@IoDispatcher` остаётся (в LEM `Dispatchers.IO` зашит в код); convention-плагины объявлены в `[plugins]` каталога; правило «feature не зависит от feature» проверяется сборкой, а не договорённостью.

Работаем этапами. После каждого этапа проект собирается и проходит тесты, и каждый этап — отдельный коммит в master.

---

## Этап 0. Зафиксировать текущие правки и сделать стартовый экран (ещё в монолите)

### Почему
В рабочем дереве лежат готовые, но незакоммиченные иконка и edge-to-edge. В `playback/PlaybackService.kt` есть **чужие правки пользователя**: их не трогаю и не включаю в свои коммиты. Перед переносом файлов их нужно закоммитить, иначе они смешаются с переносом. Стартовый экран проще доделать до разбиения: он затрагивает `SettingsRepository`, `MainActivity` и навигацию, которые потом разъедутся по разным модулям.

### 0.1 Коммиты готовой работы
- `Replace the launcher icon with a star and eternal flame`: `drawable/ic_launcher_*`, `mipmap-anydpi/`, удалённые `mipmap-*dpi`, `ic_launcher-playstore.png`.
- `Draw content behind the status bar on every screen`: `StatusBarInset.kt`, `StatusBarScrim.kt`, `TabComposable.kt` (флаг `hasStatusBarScrim`), Home, More, AdminHome и нижние отступы `bottomBarContentPadding()` на экранах, которые открываются поверх вкладок.

### 0.2 Флаг «интро уже показано»
Сейчас `NavHost` всегда стартует с `IntroScreen`, кроме запуска по ссылке, поэтому постоянный посетитель каждый раз нажимает «Начать».

`domain/repository/SettingsRepository.kt`
```kotlin
interface SettingsRepository {
    fun observeIntroSeen(): Flow<Boolean>
    suspend fun markIntroSeen()
    fun observeNotificationsAsked(): Flow<Boolean>
    suspend fun markNotificationsAsked()
    fun observeSettings(): Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setTextScale(scale: TextScale)
    suspend fun setVictoryDayReminder(isEnabled: Boolean)
    suspend fun setFavoriteReminders(isEnabled: Boolean)
}
```
`data/settings/repository/SettingsRepositoryImpl.kt` — по образцу `notifications_asked`:
```kotlin
override fun observeIntroSeen() = dataStore.data.map { it[INTRO_SEEN_KEY] == true }

override suspend fun markIntroSeen() {
    dataStore.edit { it[INTRO_SEEN_KEY] = true }
}

companion object {
    private val INTRO_SEEN_KEY = booleanPreferencesKey("intro_seen")
}
```

### 0.3 Выбор стартового экрана без мигания интро
Флаг читается из DataStore асинхронно, а `startDestination` нужен сразу. Поэтому системный splash (зависимость `core-splashscreen` уже есть) держится на экране, пока флаг не прочитан.

`ui/main/viewmodels/MainViewModel.kt`
```kotlin
val isIntroSeen = settingsRepository.observeIntroSeen()
    .map<Boolean, Boolean?> { it }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)
```
`MainActivity.onCreate`
```kotlin
val splashScreen = installSplashScreen()
super.onCreate(savedInstanceState)
val openedFromLink = intent?.data != null
splashScreen.setKeepOnScreenCondition { !openedFromLink && mainViewModel.isIntroSeen.value == null }
setContent {
    val isIntroSeen by mainViewModel.isIntroSeen.collectAsStateWithLifecycle()
    ...
    isIntroSeen?.let { seen ->
        AppNavigation(
            startRoute = if (openedFromLink || seen) Screen.HomeScreen.route else Screen.IntroScreen.route,
            onChangeLangClick = ::updateLocale
        )
    }
}
```
`res/values/themes.xml` + `AndroidManifest.xml` (`android:theme="@style/Theme.OurMemoryApp.Starting"` у `MainActivity`):
```xml
<style name="Theme.OurMemoryApp.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_background</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.OurMemoryApp</item>
</style>
```
`splash_background`: белый в `values/colors.xml`, `#151212` (`MemoryNight`) в `values-night/colors.xml`.

### 0.4 Новый стартовый экран
Сейчас на экране размытое фото, слайдшоу и два длинных абзаца, которые повторяют вкладку «О мемориале». Статус-бар при этом тёмный на тёмном фоне. Новый вид: чёткое фото на весь экран, затемнение снизу, знак звезды с огнём из иконки, название, одна строка слогана, три пункта «что умеет приложение» и широкая кнопка над навигационной панелью. Нажатие отмечает интро просмотренным.

`ui/intro/viewmodels/IntroViewModel.kt`
```kotlin
@HiltViewModel
class IntroViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun onUiIntent(intent: IntroUiIntent) {
        when (intent) {
            IntroUiIntent.OnStartClick -> viewModelScope.launch { settingsRepository.markIntroSeen() }
        }
    }
}
```
`ui/intro/models/IntroFeature.kt`
```kotlin
enum class IntroFeature(@param:DrawableRes val iconRes: Int, @param:StringRes val textRes: Int) {
    STORIES(R.drawable.person, R.string.stories_of_heroes),
    ROUTE(R.drawable.map, R.string.route_to_burial_place),
    TOURS(R.drawable.directions_walk, R.string.audio_tours)
}
```
`ui/intro/ui/IntroScreen.kt` (основа; `ImageSlideshow` и `blur` удаляются)
```kotlin
@Composable
fun IntroScreen(onStartClick: () -> Unit, introViewModel: IntroViewModel = hiltViewModel()) {
    SystemBarIcons(darkIcons = false)
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(introScrim()))
        IntroContent(
            onStartClick = {
                introViewModel.onUiIntent(IntroUiIntent.OnStartClick)
                onStartClick()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = SCREEN_PADDING, vertical = SCREEN_PADDING)
        )
    }
}

private fun introScrim() = Brush.verticalGradient(
    SCRIM_START to Color.Transparent,
    1f to Color.Black.copy(alpha = SCRIM_ALPHA)
)
```
`IntroContent` — это `Column` со знаком `R.drawable.ic_launcher_foreground` (`MARK_SIZE = 96.dp`), `app_name` (`headlineLarge`, белый), `in_memory_of_those_msg`, тремя строками `IntroFeature.entries` (иконка `MemoryRedOnDark` + текст) и `Button(fillMaxWidth, height = 56.dp)` со `start`.

Строки: новые ключи в `values/` и `values-be/`, старые `slogan` и `idea` удаляются, если больше нигде не используются.
```xml
<string name="in_memory_of_those_msg">В память о тех, кто подарил нам мирное небо</string>
<string name="stories_of_heroes">Истории героев и их награды</string>
<string name="route_to_burial_place">Путь к месту захоронения на карте</string>
<string name="audio_tours">Аудиоэкскурсии по мемориалу</string>
```
```xml
<string name="in_memory_of_those_msg">У памяць пра тых, хто падарыў нам мірнае неба</string>
<string name="stories_of_heroes">Гісторыі герояў і іх узнагароды</string>
<string name="route_to_burial_place">Шлях да месца пахавання на карце</string>
<string name="audio_tours">Аўдыяэкскурсіі па мемарыяле</string>
```
Коммит: `Show the intro only on first launch and redesign it`.

---

## Этап 1. build-logic и convention-плагины

### Почему
Без общих плагинов каждый из ~20 новых модулей повторял бы compileSdk 37.1, minSdk 28, Java 17, Compose BOM и Hilt, а версии бы разъезжались.

`settings.gradle.kts`
```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
```
`build-logic/settings.gradle.kts`
```kotlin
dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
    versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
}
rootProject.name = "build-logic"
include(":convention")
```
`build-logic/convention/build.gradle.kts`
```kotlin
plugins { `kotlin-dsl` }

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.hilt.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.ourmemory.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.ourmemory.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.ourmemory.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.ourmemory.android.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id = libs.plugins.ourmemory.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.ourmemory.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
```
`gradle/libs.versions.toml` — плагины объявлены в каталоге (в LEM этого не было):
```toml
[libraries]
android-gradle-plugin = { group = "com.android.tools.build", name = "gradle", version.ref = "agp" }
kotlin-gradle-plugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }
compose-gradle-plugin = { group = "org.jetbrains.kotlin", name = "compose-compiler-gradle-plugin", version.ref = "kotlin" }
hilt-gradle-plugin = { group = "com.google.dagger", name = "hilt-android-gradle-plugin", version.ref = "hilt" }
ksp-gradle-plugin = { group = "com.google.devtools.ksp", name = "symbol-processing-gradle-plugin", version.ref = "ksp" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
ourmemory-android-application = { id = "ourmemory.android.application" }
ourmemory-android-library = { id = "ourmemory.android.library" }
ourmemory-android-compose = { id = "ourmemory.android.compose" }
ourmemory-android-feature = { id = "ourmemory.android.feature" }
ourmemory-hilt = { id = "ourmemory.hilt" }
ourmemory-jvm-library = { id = "ourmemory.jvm.library" }
```
`build-logic/convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt`
```kotlin
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.configure<LibraryExtension> {
                configureAndroid(this)
                testOptions.unitTests.isReturnDefaultValues = true
            }
            dependencies {
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
```
`build-logic/convention/src/main/kotlin/AndroidConfiguration.kt`
```kotlin
internal fun configureAndroid(extension: CommonExtension) {
    extension.compileSdk { version = release(COMPILE_SDK) { minorApiLevel = COMPILE_SDK_MINOR } }
    extension.defaultConfig.minSdk = MIN_SDK
    extension.compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    extension.compileOptions.targetCompatibility = JavaVersion.VERSION_17
}

private const val COMPILE_SDK = 37
private const val COMPILE_SDK_MINOR = 1
private const val MIN_SDK = 28
```
`AndroidFeatureConventionPlugin.kt` подключает library, compose и hilt, стандартные зависимости фичи и **проверку, что фича не зависит от другой фичи**:
```kotlin
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.findPlugin("ourmemory-android-library").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("ourmemory-android-compose").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("ourmemory-hilt").get().get().pluginId)
            dependencies {
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:ui"))
                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
                add("testImplementation", project(":core:testing"))
            }
            forbidFeatureToFeatureDependencies()
        }
    }

    private fun Project.forbidFeatureToFeatureDependencies() {
        configurations.configureEach {
            dependencies.withType<ProjectDependency>().configureEach {
                check(!path.startsWith(FEATURE_PREFIX)) {
                    "${this@forbidFeatureToFeatureDependencies.path} must not depend on $path"
                }
            }
        }
    }

    companion object {
        private const val FEATURE_PREFIX = ":feature:"
    }
}
```
`AndroidComposeConventionPlugin` включает `buildFeatures.compose` и подключает Compose BOM, ui, material3, lifecycle-viewmodel-compose, kotlinx-immutable и coil. `HiltConventionPlugin` подключает hilt и ksp, как в LEM. `JvmLibraryConventionPlugin` подключает `kotlin-jvm` и Java 17. `AndroidApplicationConventionPlugin` задаёт то же, что сейчас в `app/build.gradle.kts` (compileSdk и minSdk, Java 17), плюс compose и hilt.

Итог этапа: `app/build.gradle.kts` использует `alias(libs.plugins.ourmemory.android.application)`, модулей пока нет, сборка зелёная.
Коммит: `Add build-logic with convention plugins`.

---

## Этап 2. Модули и правила зависимостей

### Итоговая структура
```text
:app                 MyApp (MapKit init), MainActivity, ui/main, ui/navigation (графы, нижняя панель, SessionViewModel)
:core:domain         JVM. domain/models (кроме Screen) + domain/repository
:core:common         @IoDispatcher, провайдеры Dispatchers.IO и Clock, иконка уведомлений
:core:navigation     Screen (маршруты + *_ARG), VeteranLink (app link)
:core:ui             ui/theme, ui/fonts, ui/common/**, общие строки и drawable, Reward/parseRewards,
                     FeedbackType.labelRes, TourSummaryUi/TourStopUi/ToursSheet
:core:map            MapKit: MapViewLifecycle, MapPreview, NumberImageProvider, TourMap
:core:data           все data/* на Firebase/DataStore/Retrofit + Hilt-модули из di/
:core:media          AudioRepository (Media3) + playback/PlaybackService
:core:contentcheck   data/contentcheck + assets/nsfw_mobilenet_v2_224.tflite
:core:testing        фейки domain-репозиториев, MainDispatcherRule
:sync:reminders      reminders/* (WorkManager) и привязка ReminderScheduler
:feature:intro  :feature:home  :feature:details  :feature:map  :feature:tours  :feature:info
:feature:submission  :feature:feedback  :feature:favorites  :feature:more  :feature:myrequests
:feature:admin
```
Отличие от LEM: data не дробится на `network`/`database`/`auth`. Почти все области используют один и тот же Firebase, а отдельные модули выделены только под тяжёлые библиотеки (Media3, LiteRT на 12 МБ, WorkManager), чтобы их не тянули все.

### Правила зависимостей
```text
:app ──> всё
:feature:X ──> :core:{domain,common,navigation,ui} (+ :core:map / :core:media при необходимости)
:feature:X ──X──> :feature:Y        (запрещено проверкой в AndroidFeatureConventionPlugin)
:core:data, :core:contentcheck, :core:media, :sync:reminders ──> :core:domain, :core:common
:core:ui ──> :core:domain, :core:navigation
:core:domain ──> ничего (только kotlinx-coroutines-core)
```
Фичи видят только интерфейсы из `:core:domain`. Реализации подключает `:app` через Hilt.

### Пакеты не меняются, `namespace` = корень пакета модуля
Kotlin-пакеты остаются прежними (`com.gorman.ourmemoryapp.ui.details...`, `...data.burials...`). Поэтому меняются только импорты `R`, а keep-правила в `app/proguard-rules.pro` (`domain.models.Veteran`, `data.**.model.**`) продолжают работать. У каждого модуля `namespace` — корень его пакета:

`feature/details/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.ourmemory.android.feature)
}

android {
    namespace = "com.gorman.ourmemoryapp.ui.details"
}

dependencies {
    implementation(project(":core:map"))
    implementation(project(":core:media"))
    implementation(libs.media3.exoplayer)
}
```
`core/domain/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.ourmemory.jvm.library)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
```
`settings.gradle.kts`
```kotlin
include(":app")
include(":core:domain", ":core:common", ":core:navigation", ":core:ui", ":core:map")
include(":core:data", ":core:media", ":core:contentcheck", ":core:testing")
include(":sync:reminders")
include(":feature:intro", ":feature:home", ":feature:details", ":feature:map", ":feature:tours")
include(":feature:info", ":feature:submission", ":feature:feedback", ":feature:favorites")
include(":feature:more", ":feature:myrequests", ":feature:admin")
```

### Порядок переноса (каждый шаг — зелёная сборка и коммит)
1. `:core:domain`, `:core:common`, `:core:navigation`, `:core:testing`.
2. `:core:ui` и `:core:map` вместе с разбором ресурсов (см. этап 3).
3. `:core:data`, `:core:media`, `:core:contentcheck`, `:sync:reminders`.
4. Фичи по одной, от независимых к сложным: intro → info → favorites → myrequests → feedback → submission → home → more → details → tours → map → admin.
5. Тесты разъезжаются вместе со своими классами (этап 5).

Файлы переношу через `git mv`, чтобы сохранилась история.

---

## Этап 3. Разрешение перекрёстных зависимостей

### Почему
Инвентаризация нашла четыре места, где одна фича импортирует другую. В модулях это станет зависимостью `feature → feature`, которую запрещает convention-плагин.

| Сейчас | Куда | Почему туда |
|---|---|---|
| `ui/details/models/Reward` + `parseRewards` (нужны admin/veterans) | `:core:ui`, пакет `ui.common.models` | Enum с drawable-ресурсами наград, общий для карточки и редактора |
| `ui/tours/models/TourSummaryUi`, `toSummaryUi`, `TourStopUi`, `ui/tours/ui/ToursSheet` (нужны map и admin/tours) | `:core:ui` | UI-модели экскурсий без MapKit |
| `ui/tours/ui/TourMap` (нужен admin/tours) | `:core:map` | Компонент MapKit |
| `ui/feedback/models/FeedbackTypeLabel` (`labelRes`, нужен admin/feedbacklist) | `:core:ui` | Подпись доменного enum, используется в двух фичах |
| `ui/common/models/VeteranLink` (нужен reminders) | `:core:navigation` | Это app link на `/veteran/{id}`, а не UI |

### Код, который нельзя просто перенести
`DetailsViewModel`: у `BuildConfig` библиотечного модуля нет `APPLICATION_ID`, поэтому fallback-аудио строится от `packageName` приложения. `R.raw.veteran_bio_10` переезжает в `feature/details/src/main/res/raw/`.
```kotlin
private fun fallbackAudioUri(rawResourceId: Int) =
    "$ANDROID_RESOURCE_SCHEME${context.packageName}/$rawResourceId"
```
`PlaybackService` и `ReminderNotifications` сейчас ссылаются на `MainActivity`, которая остаётся в `:app`. Открываем приложение через launch-intent пакета:
```kotlin
private fun launchAppIntent(context: Context) = PendingIntent.getActivity(
    context,
    LAUNCH_REQUEST_CODE,
    context.packageManager.getLaunchIntentForPackage(context.packageName),
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
)
```
`YandexImageResponse`: убрать `@Serializable`. Ответ разбирается Gson, а сам импорт компилировался только за счёт транзитивной зависимости, которой в `:core:data` не будет.

---

## Этап 4. Ресурсы по модулям

### Почему
При `android.nonTransitiveRClass=true` (уже включено) у каждого модуля свой `R`. Ресурс должен лежать там, где его используют, иначе фича собирается только вместе с чужими строками.

Разнос делает одноразовый скрипт в scratchpad (в репозиторий не коммитится):
1. Для каждого `<string>`, `<plurals>`, `<string-array>` и drawable/raw/font найти модули, где встречается `R.<type>.<key>` или `@<type>/<key>`.
2. Ресурс нужен одному модулю — переносится в `values/` и `values-be/` этого модуля.
3. Нужен двум и больше — переносится в `:core:ui`.
4. Не используется нигде — попадает в отчёт, и я показываю список пользователю перед удалением.

Импорт двух `R` в одном файле — через alias:
```kotlin
import com.gorman.ourmemoryapp.ui.details.R
import com.gorman.ourmemoryapp.ui.common.R as CommonR
```
`values-be` проверяется тем же скриптом: у каждого ключа из `values/` в модуле есть пара в `values-be/`.

---

## Этап 5. Тесты

### Почему
Фейки `FakeVeteransRepository`, `FakeBurialsRepository` и `FakeToursRepository` нужны и тестам `ContentEditorRepositoryImplTest` (data), и тестам view model (фичи).

- `:core:testing`: фейки domain-интерфейсов и `MainDispatcherRule`. Фичи получают его через `AndroidFeatureConventionPlugin`, `:core:data` — через `testImplementation(project(":core:testing"))`.
- Фейки data-источников (`FakeAuthRemoteDataSource` и другие) остаются в `core/data/src/test`.
- Каждый тест переезжает в `src/test` модуля своего класса. `VeteranAnniversariesTest` идёт в `:core:domain`, `VictoryDayReminderTimeTest` — в `:sync:reminders`.

`core/testing/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.ourmemory.android.library)
}

android {
    namespace = "com.gorman.ourmemoryapp.testutil"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
}
```

---

## Этап 6. Detekt, R8 и документация

- `detektAll` сканирует `file(projectDir)` и сам подхватывает новые модули. Записи в `config/baseline.xml` привязаны к имени файла, а не к пути, поэтому переносы их не ломают. Устаревшие записи (`OurMemoryScreen.kt`, `OurMemoryViewModel.kt`, `Images.kt` и другие) чистятся через `detektGenerateBaseline`.
- R8 работает в `:app` по всему графу. Keep-правила остаются в `app/proguard-rules.pro`, потому что пакеты не меняются. `androidResources { noCompress += "tflite" }` тоже остаётся в `:app`: упаковка APK происходит там.
- `CLAUDE.md`:
  - «Project»: многомодульный проект вместо `:app`, убрать Lottie.
  - «Commands»: тесты модуля `./gradlew :feature:map:testDebugUnitTest`, все тесты `./gradlew testDebugUnitTest`.
  - «Architecture»: карта модулей, правила зависимостей, `namespace` = корень пакета, ресурсы по модулям с `CommonR`.
  - «Package structure»: тот же принцип внутри модуля.
  - Убрать устаревшее «always-light scheme»: в приложении есть тёмная тема.
- Коммит: `Split the app into core and feature modules`, по одному коммиту на каждый шаг этапа 2.

---

## Проверка

После каждого этапа:
```bash
./gradlew detektAll
./gradlew testDebugUnitTest
./gradlew assembleDebug
```
В конце:
```bash
./gradlew assembleRelease
./gradlew :feature:map:dependencies --configuration debugRuntimeClasspath
./gradlew installDebug
```
- Последняя команда `dependencies` подтверждает, что в графе `:feature:map` нет других `:feature:*`.
- Контрольная проверка правила: временно добавить `implementation(project(":feature:tours"))` в `:feature:map`. Сборка должна упасть с сообщением `must not depend on`, после чего строка удаляется.

Ручные сценарии на телефоне (debug и release):
1. **Первый запуск** после очистки данных: splash с иконкой, затем новый стартовый экран, «Начать», вкладка «Ветераны». Повторный запуск открывает сразу «Ветеранов» без мигания интро.
2. **Запуск по ссылке** `https://chatroom-85fb8.web.app/veteran/10` открывает карточку ветерана.
3. **Вкладки:** контент уходит под полупрозрачную полосу статус-бара, нижние элементы не прячутся под панелями.
4. **Карточка ветерана:** аудиобиография (в том числе fallback для `"10"`), уведомление плеера открывает приложение, свеча, избранное, фото.
5. **Карта и экскурсии:** кластеры, лист экскурсий, маршрут, озвучка остановок.
6. **Обращения:** проверка мата и фото (модель читается из assets `:core:contentcheck`), «Мои обращения».
7. **Админка:** вход, все редакторы (награды ветерана, карта в редакторе экскурсии), модерация, обратная связь.
8. **Напоминания:** тестовый запуск воркеров, уведомление открывает приложение.
9. **Язык be:** все экраны на белорусском, ни одного ключа без перевода (строки теперь в разных модулях).
10. **Release:** R8 ничего не вырезал (Firebase-модели, Gson, LiteRT).
