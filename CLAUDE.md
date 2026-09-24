# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

OurMemoryApp is a single-module (`:app`) Android app for digitizing a historical military cemetery (a college project). It lists veterans buried there, shows a detail page per veteran (bio, rewards, photos, audio biography) and has a Yandex MapKit map. Stack: Kotlin, Jetpack Compose, Material 3, Hilt (KSP), Firebase Realtime Database, Retrofit, Coil, Lottie, kotlinx-collections-immutable. Dependency versions are in `gradle/libs.versions.toml`.

## Commands

Use the Gradle wrapper (`./gradlew`) for everything — do not invoke a system-installed `gradle`.

```bash
./gradlew assembleDebug          # build debug APK
./gradlew build                  # full build (compile + lint + assemble)
./gradlew installDebug           # install on a connected device/emulator
./gradlew assembleRelease        # minified + resource-shrunk; signed only if keystore.properties exists
./gradlew detektAll              # Detekt static analysis (+ detekt-formatting) over the whole project
./gradlew detektAll -PdetektAutoFix   # same, with autocorrect
./gradlew detektGenerateBaseline # regenerate config/baseline.xml
```

QR codes and app links (not part of the Gradle build):

```bash
python3 -m venv .venv && .venv/bin/pip install -r tools/qr/requirements.txt
.venv/bin/python tools/qr/generate_qr_sheet.py --output qr_sheet.pdf   # printable QR cards from the live database
cd firebase && firebase deploy --only hosting                            # assetlinks.json + veteran web page
```

Detekt uses `config/detekt.yml` with `maxIssues: 0`, so any new finding not in `config/baseline.xml` fails the task. There are currently no unit or instrumented tests in the repo. If you add tests, put JVM tests under `app/src/test/...` and instrumented tests under `app/src/androidTest/...` (standard AGP layout). Run `./gradlew detektAll` before finishing any change that touches Kotlin source.

## Local configuration

- `local.properties` must define `MAPKIT_API_KEY`. The root `build.gradle.kts` reads it into `extra["mapkitApiKey"]`, the app exposes it as `BuildConfig.MAPKIT_API_KEY`, and `MyApp` passes it to `MapKitFactory` on startup.
- `app/google-services.json` configures Firebase.
- Release signing reads an optional root `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`).

## Architecture

Package root: `app/src/main/java/com/gorman/ourmemoryapp/`, split into `data/`, `domain/`, `di/`, `ui/`.

**Data flow.** All veteran data comes from the Firebase Realtime Database node `"Veterans"`. `FirebaseDBImpl` does a one-shot `get()` wrapped in `suspendCoroutine` and deserializes each child into `domain.models.Veteran`, so `Veteran` needs default values for every field. `VeteransRepository` has no cache: both `HomeViewModel` and `DetailsViewModel` call `getAllVeterans()` and filter locally. The detail screen finds a veteran by `id` in the full list.

**Yandex Disk images.** `Veteran.veteransInfo` is a list of mixed strings. `DetailsViewModel` splits them up:
- Entries containing `http` are media links, optionally written as `url|description`.
- Everything else is a text paragraph.

Links containing `yandex` are public Yandex Disk links. They are resolved to direct download hrefs through `YandexApiService` (Retrofit, base URL `https://cloud-api.yandex.net/`, `v1/disk/public/resources/download`). If resolution fails, the original link is kept.

**Rewards.** `Veteran.rewards` is a comma-separated string of integer IDs. The ID → name mapping (in Russian) is in `app/src/main/assets/rewards_rules.txt`.

**Audio.** `AudioRepository` (in `data/repository`, injected directly with no domain interface) wraps a single `MediaPlayer` and exposes `playbackState: StateFlow<AudioPlaybackState>`. Audio files are raw resources. `DetailsViewModel.loadAudioForVeteran` currently returns the same `R.raw.veteran_bio_10` for every veteran; it is a placeholder `when` that is meant to be extended per veteran.

**DI.** Everything is provided as a singleton in `di/AppModule.kt`: the Firebase DB and `Veterans` reference, `FirebaseDB`, `VeteransRepository`, Retrofit, and `YandexApiService`.

**UI / state pattern** (MVI-flavored MVVM):
- Every screen has a sealed `*UiState` (`Loading` / `Success` / `Error`) and a sealed input type in `ui/states/`: `HomeUiIntent` handled by `onUiIntent`, and `DetailsUiEvent` / `AudioAction` handled by `onUiEvent`.
- ViewModels build `uiState` as a cold flow turned into state with `stateIn(viewModelScope, WhileSubscribed(5000), Loading)`. `HomeViewModel` `combine`s the search, War, and Art filter `MutableStateFlow`s; `Veteran.category` is `"War"` or `"Art"`.
- Collections in UI state use `ImmutableList` / `ImmutableMap` from kotlinx-collections-immutable, which keeps them stable for Compose. Follow this for new state.
- `DetailsViewModel` uses Hilt assisted injection (`@HiltViewModel(assistedFactory = ...)`) to receive `veteranId`. `AppNavigation` creates it with `hiltViewModel<DetailsViewModel, DetailsViewModel.Factory> { it.create(id) }`.

**Navigation.** `ui/AppNavigation.kt` uses a Compose `NavHost` with routes from the `Screen` sealed class (defined in `domain/models/VeteransModel.kt`): Intro → Home (Intro is popped) → `detailscreen/{veteranId}`, plus Info.

**Localization.** Strings are in `values/` (default, Russian) and `values-be/` (Belarusian). The language switch on InfoScreen calls `MainActivity.updateLocale`, which updates the resources configuration and recreates the activity.

## Team Conventions

Much of the existing code predates these rules (flat `ui/screens`, `ui/viewModel`, `ui/states` packages; several types per file; comments; inline numbers; some hardcoded strings). Apply the rules to all new and touched code; don't mass-refactor untouched code unless asked.

### Files

Save all Claude-generated documents (plans, review summaries, task lists) in the `claude/` folder at the repo root (create it if missing). Plans must be written in Russian and saved as `.md` files there, named `claude/<topic>-plan.ru.md`. Write the plan file into `claude/` as the **first** action after a plan is approved, before any code is touched — a plan that only exists in the chat is not delivered.

### Plan content

Every plan must be self-explanatory. For each meaningful step, state **why** the change is needed (what is missing or breaks without it) and show a **code example** (Kotlin, XML, TOML) of the resulting code — not a prose description of it. A step that only names the files to touch is not an acceptable plan step.

Bad:

```markdown
- Привязать аудио к ветерану.
```

Good:

````markdown
### Почему

`DetailsViewModel.loadAudioForVeteran` возвращает один и тот же `R.raw.veteran_bio_10` для любого
`veteranId`, поэтому у каждого ветерана звучит чужая биография: аудио нужно выбирать по ID ветерана.

### Код

```kotlin
private fun loadAudioForVeteran(veteranId: String): AudioItem? {
    val rawResourceId = veteranAudioResources[veteranId] ?: return null
    return AudioItem(
        id = rawResourceId,
        title = context.getString(R.string.veteran_biography),
        fileName = veteranId,
        rawResourceId = rawResourceId,
        itemId = rawResourceId
    )
}
```
````

Code examples in plans follow the same rules as production code (one type per file, no comments, named constants, string resources). A plan must also end with a **verification** section listing the build/lint commands and the manual scenarios that prove the feature works end to end.

### Mindset

Do not be a yes-man. If a proposed approach has problems, say so and explain the trade-off before implementing. State your position first; implement what the user decides after the discussion.

### Commit messages

A commit message is a general one-line summary of the feature (or features) the commit delivers, then a blank line, then a bullet list with one short sentence per feature. Keep the summary at the level of what the commit does as a whole, not a file-by-file changelog.

```text
Add audio biography playback on the details screen

- Play, pause and seek a veteran's audio biography
- Stop playback when leaving the details screen
```

Never mention Claude, Claude Code, or any AI assistant in a commit: no `Co-Authored-By: Claude ...` trailer, no `Generated with Claude Code` line, no emoji marker. The same applies to pull request descriptions.

### Package structure

Never flat-pack every file of a feature into one folder. Group by feature first, then split each feature into sub-packages by the role each file plays.

In **`ui/`** (this project's presentation layer) each feature package (`home/`, `details/`, `info/`, `intro/`) uses these sub-packages:

- `ui/` — composables (`*Screen.kt`, reusable components) and the feature's navigation code;
- `viewmodels/` — `@HiltViewModel` classes;
- `models/` — UI state (`*UiState.kt`), intents/events (`*UiIntent.kt`, `*UiEvent.kt`), UI models (`*Ui.kt`) with their `toExternalModel()` mappers, UI-only enums/constants;
- any further sub-package the feature genuinely needs (`components/`, `mappers/`, …) when a role grows large enough to stand on its own.

```text
ui/details/
├── models/
│   ├── AudioAction.kt
│   ├── DetailsUiEvent.kt
│   └── DetailsUiState.kt
├── ui/
│   └── DetailsScreen.kt
└── viewmodels/
    └── DetailsViewModel.kt
```

The same principle applies to **`domain/`** and **`data/`**: group files by meaning, not in one pile. `domain/` splits into `usecase/` and `model/` (plus `repository/` for interfaces); `data/` splits per feature area and then by role — `repository/`, `datasource/local/`, `datasource/remote/`, `model/` (DTOs/entities), `mapper/`.

```text
data/veterans/
├── datasource/
│   └── remote/
│       ├── FirebaseDB.kt
│       ├── FirebaseDBImpl.kt
│       └── YandexApiService.kt
├── mapper/
│   └── YandexImageMapper.kt
├── model/
│   └── YandexImageResponse.kt
└── repository/
    └── VeteransRepositoryImpl.kt
```

When adding a file to an existing feature that is still flat, create the missing sub-package for it rather than extending the flat layout.

### Kotlin code rules

- **One type per file**: every `class`, `data class`, `sealed class`, `sealed interface`, `enum class`, `interface`, and `object` lives in its own file named after the type. The only exceptions are small private helper types used exclusively by one other type in the same file.
- **No comments**: no `//`, `/* */`, or KDoc (`/** */`) in Kotlin source. Self-documenting names are the only acceptable form of documentation.
- **Naming**: ordinary variables, properties and functions are plain `camelCase`. The leading underscore is reserved for one thing — the backing private mutable half of a mutable/immutable pair (`StateFlow`, `LiveData`, `MutableList` exposed as `List`, …): the private mutable property is `_camelCase` and the public immutable one it backs carries the same name without the underscore. Never write `_name` for a private property that has no public immutable counterpart (e.g. `FirebaseDBImpl._dbRef` should be `dbRef`).

  ```kotlin
  private val _playbackState = MutableStateFlow(AudioPlaybackState())
  val playbackState = _playbackState.asStateFlow()
  ```
- **Inferred types**: never write a type annotation the compiler can infer from the initializer. Declare `val uiState = _uiState.asStateFlow()`, not `val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()` — `asStateFlow()` (and `asSharedFlow()`, `stateIn()`, `toList()`, …) already returns the immutable type. Keep the annotation only where it actually changes the inferred type: when the initializer would infer the mutable or a narrower type than the one to expose (`val items: List<Veteran> = mutableListOf()`), or on an explicitly nullable/upcast property.
- **Named constants**: all numeric values that carry meaning (timeouts, limits, IDs, thresholds, etc.) must be `private const val` inside the owning type's `companion object`. Never write a raw number inline where the value carries meaning (e.g. `WhileSubscribed(5000L)` → `WhileSubscribed(STOP_TIMEOUT_MILLIS)`).
- **No redundant `return@label`**: never label-return the last expression of a lambda — the trailing expression is already its value. `return@label` is reserved for an early exit from the middle of a lambda (`?: return@mapNotNull null`, a guard inside an `if`).

  ```kotlin
  infoList.mapNotNull { info ->
      val url = info.split(URL_SEPARATOR).firstOrNull() ?: return@mapNotNull null
      url.trim()
  }
  ```
- **No `init {}` blocks**: never kick work off from an `init {}` block in a ViewModel (or anywhere else) — it starts loading before anything collects, keeps running when the UI is gone, and re-runs nothing after process death. Expose state declaratively instead: build the flow in a private `observeXxxUiState()` function and turn it into a `StateFlow` with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(...), <initial state>)`, so collection starts when the UI subscribes and stops with it. One-shot loads are modelled as a flow (`flow { }`, `onStart { }`, `asFlow()`) inside that chain rather than a `viewModelScope.launch` in `init`. `viewModelScope.launch` stays legal only for user-triggered actions in event handler functions.

  ```kotlin
  @HiltViewModel
  class HomeViewModel @Inject constructor(
      private val repository: VeteransRepository
  ) : ViewModel() {

      private val searchState = MutableStateFlow("")

      val uiState = observeHomeUiState().stateIn(
          viewModelScope,
          SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
          HomeUiState.Loading
      )

      private fun observeHomeUiState(): Flow<HomeUiState> {
          return searchState.map { search ->
              HomeUiState.Success(
                  veterans = repository.getAllVeterans()
                      .filter { it.name.contains(search, ignoreCase = true) }
                      .toPersistentList(),
                  search = search
              )
          }
      }

      companion object {
          private const val STOP_TIMEOUT_MILLIS = 5000L
      }
  }
  ```
- **Localization**: never hardcode user-facing strings (e.g. the `"Биография ветерана"` title in `DetailsViewModel`). Every string shown in the UI must go through Android string resources (`context.getString(R.string.key)` / Compose `stringResource(R.string.key)`), with the key added to both `values/strings.xml` (Russian) and `values-be/strings.xml` (Belarusian).
- **String resource naming**: the key mirrors the string's own content (in English), lowercased and snake_cased — never a category/role prefix like `details_audio_title` or a `_title`/`_label` suffix. When the content is too long to spell out in full, take the first few meaningful words and append `_msg`.

  ```xml
  <string name="veteran_biography">Биография ветерана</string>
  <string name="search_by_name">Поиск по имени</string>
  <string name="veteran_not_found_msg">Не удалось найти информацию о ветеране. Попробуйте позже</string>
  ```

  Two strings whose content differs only by context still get distinct keys built from their own words (`search` vs `search_by_name`), not from where they are used.
