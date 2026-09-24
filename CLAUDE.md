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

Detekt uses `config/detekt.yml` with `maxIssues: 0`, so any new finding not in `config/baseline.xml` fails the task. Unit tests live in `app/src/test/...` (fakes in `testutil/`, `MainDispatcherRule` for view models): `./gradlew testDebugUnitTest`, a single class with `./gradlew testDebugUnitTest --tests '*MapViewModelTest'`. There are no instrumented tests yet (`app/src/androidTest/...` if added). Run `./gradlew detektAll` before finishing any change that touches Kotlin source.

## Local configuration

- `local.properties` must define `MAPKIT_API_KEY`. The root `build.gradle.kts` reads it into `extra["mapkitApiKey"]`, the app exposes it as `BuildConfig.MAPKIT_API_KEY`, and `MyApp` passes it to `MapKitFactory` on startup.
- `app/google-services.json` configures Firebase.
- Release signing reads an optional root `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`).

## Architecture

Package root: `app/src/main/java/com/gorman/ourmemoryapp/`:
- `data/` is split per area: `burials/`, `tours/`, `candles/`, `settings/`, `submissions/`, plus the older `datasource/` and `repository/` for veterans and audio.
- `domain/` holds `models/` and `repository/` interfaces.
- `di/` holds Hilt modules.
- `ui/` holds one package per feature (`home`, `intro`, `details`, `map`, `tours`, `info`, `submission`, `navigation`), each with `models/`, `ui/` and `viewmodels/`. `ui/common` holds shared composables and UI models.
- `reminders/` holds the yearly May 9 WorkManager job.

**Firebase data** (Realtime Database). The database is shared with other apps (`ChatRoom`, `FitnessApp`), so everything this app uses lives under one node, `OurMemory`. The node names are in `data/firebase/DatabaseNodes.kt`. Data sources receive the `@MemoryRoot DatabaseReference` and call `root.child(...)`. Every model needs defaults on all fields for deserialization.

| Node under `OurMemory/` | Contents | Accessed by |
|---|---|---|
| `Veterans/veteran{id}` | `id`, `name`, `portrait`, `years`, `category` (`"War"` / `"Art"`), `rewards`, `veteransInfo`, `burialId`, `audioUrl`, `birthDate` / `deathDate` (`yyyy-MM-dd`) | read by everyone; written whole by the admin veteran editor through `ContentEditorRepository`, which then calls `VeteransRepository.invalidate()` |
| `Burials/{id}` | a grave, mass grave or monument, with coordinates and section/row/place | read by everyone; written by the admin burial editor (new ids come from `push().key`) |
| `Tours/{id}` | ordered `stops` that point to a `burialId`, with `text` and `audioUrl` | read by everyone; written by the admin tour editor |
| `Candles/{veteranId}` | a counter | incremented in a transaction |
| `Submissions` | relatives' materials, `status` `pending` / `approved` / `rejected` | written after anonymous auth; photos go to Storage `OurMemory/Submissions/{id}/`. Admins approve with one `updateChildren` that appends text and photos to `Veterans/veteran{id}/veteransInfo` |
| `Feedback` | visitors' messages and error reports (`type`, `text`, `contact`, optional `veteranId`, `status` `new` / `done`) | written after anonymous auth, read and marked reviewed by admins |
| `Admins/{uid}` | `true` for every administrator account | read by `AuthRepository` to decide the role |

Other data facts:
- `VeteransRepositoryImpl`, `BurialsRepositoryImpl` and `ToursRepositoryImpl` load each node once per process and cache it behind a `Mutex`, so screens filter locally. `invalidate()` drops the cache; `ContentEditorRepositoryImpl` calls it after every admin write.
- Offline persistence is enabled on the `FirebaseDatabase` provider.
- Submissions and feedback carry `authorUid`, and admins may add `reply` and `reviewedAt`; visitors see them under More → «Мои обращения» (`MyRequestsRepository`). The anonymous uid lives until the app is reinstalled.
- Live listeners go through `DatabaseReference.observeValue()` (`data/firebase/DatabaseReferenceFlows.kt`) so errors reach the flow instead of the main thread. Visitor writes call `AnonymousSession.ensureSignedIn()` first. Parse lists with `DataSnapshot.childrenAs<T>()`, which skips malformed children, and build veteran keys with `VeteranKeys.forId`.
- `firebase/database.rules.json` and `firebase/storage.rules` cover only `OurMemory` and are **not** wired into `firebase.json`. Deploying them would replace the rules of the other apps, so merge them by hand in the console.
- Rules: everyone reads `Veterans`, `Burials`, `Tours`; only uids listed in `Admins` write them and read `Submissions` / `Feedback`; visitors may only create new submissions and feedback (with `authorUid` equal to their uid) and read their own through an `orderByChild("authorUid").equalTo(uid)` query. Storage rules cannot read the database, so admin uids are listed in `isAdmin()` in `firebase/storage.rules` (replace `ADMIN_UID`). `OurMemory/Media/**` is public for reading.
- Setting up an admin: enable Email/Password in Firebase Authentication, create the user, add `OurMemory/Admins/{uid}: true` in the console and put the uid into `storage.rules`.

**Parsing veteran content.**
- `veteransInfo` mixes paragraphs and media links. Entries containing `http` are links, optionally written as `url|description`.
- `VeteransRepository.resolveDirectUrl` turns public Yandex Disk links into direct hrefs, and keeps the original link on failure.
- `rewards` is a comma-separated list of IDs. `parseRewards` maps it to the `Reward` enum and groups repeated rewards into `×N`.

**Audio.** `AudioRepository` wraps a Media3 `ExoPlayer` and plays by URL, including `android.resource://` URIs. It is created per ViewModel, which must call `release()` in `onCleared`. A veteran's audio comes from `audioUrl`. The bundled `R.raw.veteran_bio_10` is only a fallback for veteran `"10"`.

**Maps (Yandex MapKit).**
- `rememberMapViewWithLifecycle` starts and stops `MapView` with the screen lifecycle.
- MapKit keeps tap, cluster and location listeners as **weak references**, so always create them inside `remember`.
- Burial markers are clustered with `ClusterizedPlacemarkCollection`. `NumberImageProvider` draws both cluster counts and tour stop numbers.

**UI / state pattern** (MVI-flavored MVVM):
- Every screen has a sealed `*UiState` and a sealed `*UiIntent` or `*UiEvent`.
- ViewModels build state declaratively with `stateIn(viewModelScope, WhileSubscribed(5000), Loading)`. They use `@IoDispatcher` for `flowOn` so tests can substitute a dispatcher.
- Collections in UI state use `ImmutableList` / `ImmutableMap`.
- `DetailsViewModel` uses assisted injection for `veteranId`. The other screens read route arguments from `SavedStateHandle`.

**Navigation and chrome.**
- `ui/navigation/ui/AppNavigation.kt` hosts a `Scaffold` with bottom tabs (`TopLevelTab`: veterans, map, about, admin) that are shown only on tab roots. The admin tab is shown only while `SessionViewModel.isAdmin` is true.
- Admins sign in with e-mail and password (Firebase Auth) from the About tab. A user is an admin only when a non-anonymous account has a record in `Admins/{uid}`; `AuthRepository.signIn` signs out any other account. Admin routes live in `ui/navigation/ui/AdminGraph.kt`, admin features in `ui/admin/*`. Admin media (portraits, photos, audio) is uploaded by `MediaRepository` to Storage `OurMemory/Media/{folder}/`.
- Pushed routes: `detailscreen/{veteranId}` (also the app link `https://chatroom-85fb8.web.app/veteran/{id}`), `burialmap/{burialId}`, `tour/{tourId}`, `submission/{veteranId}` and `feedback?veteranId={veteranId}` (veteran is optional). Veteran-card routes live in `ui/navigation/ui/VeteranGraph.kt`.
- When the app is opened from a link, it starts on home instead of the intro.
- The app is edge-to-edge with an always-light scheme. Hero screens overlay `FloatingTopBar` (a circle back button and a centered title once scrolled) and toggle status bar icon color with `SystemBarIcons`.

**Localization.** Strings are in `values/` (default, Russian) and `values-be/` (Belarusian); both must be updated together. The language switch on the About tab calls `MainActivity.updateLocale`, which recreates the activity.

## Team Conventions

Some older code still predates these rules (e.g. `data/datasource`, `IntroScreen`, `YandexImageResponse` with its `@Serializable` import). Apply the rules to all new and touched code; don't mass-refactor untouched code unless asked.

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
