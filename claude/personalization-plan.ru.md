# Персонализация, вкладка «Ещё», тёмная тема, плавающий бар, QR-сканер

## Контекст

Админка готова (коммиты `8d4b2ed`…`3573b47`). Теперь нужно улучшить опыт посетителя. Решения пользователя:
- **связь и персонализация:** «Мои обращения» с ответом админа, избранные ветераны с напоминаниями, размер шрифта, прогресс экскурсий;
- **профиль:** вкладка «Ещё» без аккаунта (настройки, мои обращения, избранное, вход для админа);
- **языки:** английский и китайский пока не добавляем;
- **в эту итерацию также входят:** плавающий нижний бар в стиле iOS, тёмная тема, кнопка QR-сканера, инструкция для команды.

Первое действие после одобрения — сохранить план в `claude/personalization-plan.ru.md`. Каждый шаг: `./gradlew assembleDebug detektAll testDebugUnitTest` и коммит в `master`. Новые строки — в `values/` и `values-be/`.

## Шаг 1. Настройки в DataStore и их применение

**Почему.** Тема, размер шрифта, язык и напоминания должны переживать перезапуск. Сейчас `MainActivity.updateLocale` меняет язык только до перезапуска процесса, а тема всегда светлая.

```kotlin
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textScale: TextScale = TextScale.NORMAL,
    val victoryDayReminder: Boolean = true,
    val favoriteReminders: Boolean = true
)
```

```kotlin
enum class TextScale(val factor: Float) {
    NORMAL(1f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f)
}
```

`SettingsRepository` получает `observeSettings()`, `setThemeMode`, `setTextScale`, `setVictoryDayReminder`, `setFavoriteReminders`. `MainViewModel` отдаёт `AppSettings` в `MainActivity`:

```kotlin
val density = LocalDensity.current
CompositionLocalProvider(
    LocalDensity provides Density(density.density, density.fontScale * settings.textScale.factor)
) {
    OurMemoryAppTheme(darkTheme = settings.themeMode.isDark(isSystemInDarkTheme())) { AppNavigation(...) }
}
```

Язык переводим на `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))`. Для этого `MainActivity` наследуется от `AppCompatActivity` (тема `Theme.AppCompat.DayNight.NoActionBar`), в манифест добавляется `AppLocalesMetadataHolderService` с `autoStoreLocales`, а также `res/xml/locales_config.xml` (ru, be). Выбор сохраняется системой, самописный `updateLocale` удаляется. Выключенный переключатель «9 Мая» отменяет работу через `WorkManager.cancelUniqueWork`.

## Шаг 2. Тёмная тема

**Почему.** Сейчас `lightColorScheme` включён всегда, а `SystemBarIcons` и `enableEdgeToEdge` жёстко рассчитаны на светлый фон.

```kotlin
private val MemoryDarkColorScheme = darkColorScheme(
    primary = MemoryRedOnDark,
    onPrimary = MemoryInk,
    primaryContainer = MemoryRedContainerDark,
    background = MemoryNight,
    surface = MemoryNight,
    surfaceVariant = MemoryNightHigh,
    onBackground = MemoryPaperLow,
    onSurface = MemoryPaperLow,
    onSurfaceVariant = MemoryInkMutedOnDark
)
```

- `SystemBarIcons(darkIcons = darkIcons && !isDark)`: на тёмном фоне иконки всегда светлые.
- `enableEdgeToEdge` использует `SystemBarStyle.auto(...)`.
- Карта: `map.isNightModeEnabled = isDark` в `ui/common/ui/MapViewLifecycle.kt` (там создаётся `MapView`), чтобы тема затронула все карты.
- Пройтись по экранам на предмет захардкоженных `Color.White` / `Color.Black`, скрим героев оставить тёмным.

## Шаг 3. Плавающий нижний бар

**Почему.** Нужен вид «пилюли» над контентом, как в iOS. Сейчас `NavigationBar` во всю ширину отрезает контент снизу.

```kotlin
Surface(
    shape = RoundedCornerShape(percent = 50),
    shadowElevation = 8.dp,
    color = MaterialTheme.colorScheme.surface.copy(alpha = BAR_ALPHA),
    modifier = modifier
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 8.dp)
) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.height(BAR_HEIGHT)) {
        tabs.forEach { tab -> FloatingTabItem(tab = tab, isSelected = tab == selectedTab, onClick = { onTabClick(tab) }) }
    }
}
```

`AppNavigation` рисует `NavHost` на весь экран, а бар кладёт поверх в `Box` с `Alignment.BottomCenter`. Высоту бара со всеми отступами экраны-вкладки получают через `LocalBottomBarInset` (`CompositionLocal`) и добавляют её в `contentPadding` списков. У карты на ту же величину поднимаются элементы управления.

## Шаг 4. Вкладка «Ещё»

**Почему.** Настройкам, избранному и «Моим обращениям» нужно общее место. На «О кладбище» они смешались бы с информацией о кладбище.

`TopLevelTab.MORE(Screen.MoreScreen, R.drawable.settings, R.string.more)`. Порядок вкладок: Ветераны, Карта, О кладбище, Ещё и «Админ», если пользователь — администратор. Экран `ui/more/`:
- Мои обращения (со значком непрочитанных ответов);
- Избранное;
- Сканировать QR-код;
- Оформление: тема (системная / светлая / тёмная), размер текста;
- Язык: русский / беларуская;
- Напоминания: 9 Мая, памятные дни избранных;
- Вход для администратора (переносится с «О кладбище», как и «Написать нам»).

```kotlin
sealed interface MoreUiIntent {
    data class OnThemeModeChange(val mode: ThemeMode) : MoreUiIntent
    data class OnTextScaleChange(val scale: TextScale) : MoreUiIntent
    data class OnLanguageChange(val languageTag: String) : MoreUiIntent
    data class OnVictoryDayReminderChange(val isEnabled: Boolean) : MoreUiIntent
    data class OnFavoriteRemindersChange(val isEnabled: Boolean) : MoreUiIntent
}
```

## Шаг 5. «Мои обращения» и ответ админа

**Почему.** Сейчас посетитель отправляет заявку или сообщение и больше ничего не узнаёт. Анонимный uid живёт на устройстве до переустановки, по нему можно найти свои записи.

- При создании в `Submissions` и `Feedback` пишется `authorUid = auth.uid`.
- Админ пишет `reply` и `repliedAt`: в карточке обращения появляется поле «Ответ», в разборе заявки — необязательная «Причина / комментарий».
- Посетитель читает свои записи запросом:

```kotlin
root.child(DatabaseNodes.FEEDBACK).orderByChild("authorUid").equalTo(uid).observeValue()
```

Правило разрешает такой запрос только для своего uid:

```json
".read": "auth != null && (root.child('OurMemory/Admins/' + auth.uid).exists() || (query.orderByChild === 'authorUid' && query.equalTo === auth.uid))"
```

- В `database.rules.json` добавляется `.indexOn: ["authorUid"]`, а `.validate` для посетителя требует `authorUid === auth.uid`.
- Экран `ui/mysubmissions/`: общий список заявок и обращений с датой, статусом и ответом.
- Время последнего просмотра хранится в DataStore, по нему считается значок на пункте «Мои обращения».
- Старые записи без `authorUid` в список не попадут, это ожидаемо.

## Шаг 6. Избранные ветераны и напоминания

**Почему.** Родственники возвращаются к «своим» ветеранам. Напоминание в день рождения или день памяти — главный повод открыть приложение снова.

- Хранилище: `DataStore` `stringSetPreferencesKey("favorite_veterans")`, `FavoritesRepository` с методами `observeFavorites()` и `toggle(id)`.
- Сердечко в `actions` у `FloatingTopBar` на карточке ветерана, список «Избранное» во вкладке «Ещё».
- `FavoriteAnniversaryWorker` запускается раз в день (`PeriodicWorkRequest`, уникальная работа). Он берёт избранных и через ту же логику дат, что `ui/home/models/Anniversaries.kt`, выбирает сегодняшние дни рождения и дни памяти. Затем показывает уведомление, по нажатию на которое открывается `detailscreen/{id}`. Логику дат нужно вынести из `ui/home/models` в `domain`, чтобы её мог использовать и Worker.

```kotlin
fun Veteran.anniversaryOn(date: LocalDate): AnniversaryKind? = when {
    birthDate.toIsoDateOrNull()?.let { MonthDay.from(it) } == MonthDay.from(date) -> AnniversaryKind.BIRTHDAY
    deathDate.toIsoDateOrNull()?.let { MonthDay.from(it) } == MonthDay.from(date) -> AnniversaryKind.MEMORY_DAY
    else -> null
}
```

## Шаг 7. Прогресс экскурсий

**Почему.** Экскурсию проходят ногами, её часто прерывают. Нужно видеть, где остановился, и продолжить с того же места.

- DataStore: `stringSetPreferencesKey("tour_progress_$tourId")` хранит индексы пройденных остановок.
- `TourUiIntent.OnStopVisitedToggle(index)` и `OnResetProgress`. В `TourStopRow` появляется отметка «Пройдено».
- В `ToursSheet` отображается «2 из 4». Экскурсия открывается с выбранной первой непройденной остановкой.

## Шаг 8. Кнопка QR-сканера

**Почему.** Системная камера и так открывает app link с QR на могиле, но многие посетители этого не знают. Google Code Scanner не требует разрешения на камеру и своего UI.

`libs.versions.toml`: `play-services-code-scanner`. Кнопка в шапке «Ветеранов» и пункт во вкладке «Ещё».

```kotlin
fun VeteranLink.parseVeteranId(rawValue: String): String? =
    rawValue.takeIf { it.startsWith("$BASE_URL/") }?.removePrefix("$BASE_URL/")?.substringBefore('?')?.takeIf { it.isNotBlank() }
```

Для чужого кода показывается snackbar «Это не QR-код нашего кладбища». Тест: разбор ссылки, чужого URL и пустой строки.

## Шаг 9. Инструкция для команды

**Почему.** Команда будет добавлять контент и озвучку без разработчика. Нужна одна страница, которую можно отправить ссылкой.

Публикуется как страница (Artifact), копия лежит в `claude/`. Содержание:
1. Как получить доступ: владелец создаёт аккаунт, добавляет `OurMemory/Admins/{uid}` и uid в `storage.rules`.
2. Порядок работы: сначала место захоронения (координаты у могилы), затем ветеран (выбор места), затем экскурсия (из мест).
3. Фото, биография, награды, даты — что куда вводить.
4. Озвучка: Suno генерирует песни, а не чтение текста. Для биографий нужен TTS (например, ElevenLabs или Yandex SpeechKit с русским голосом). Экспортировать mp3 до 50 МБ и загрузить через «Добавить аудио».
5. Модерация заявок и ответы на обращения.
6. Печать QR-табличек: `tools/qr/generate_qr_sheet.py`.

## Затрагиваемые файлы

- `MainActivity.kt`, `AndroidManifest.xml`, `res/xml/locales_config.xml`, `ui/theme/{Theme,Color}.kt`, `ui/common/ui/{SystemBarIcons,MapViewLifecycle}.kt`
- `domain/repository/SettingsRepository.kt`, `data/settings/repository/SettingsRepositoryImpl.kt`, новые `domain/models/{AppSettings,ThemeMode,TextScale}.kt`
- `ui/navigation/{models/TopLevelTab.kt, ui/AppNavigation.kt, ui/AppBottomBar.kt}`, новый `ui/more/`
- `data/{feedback,submissions,moderation}/…` (`authorUid`, `reply`), новый `ui/mysubmissions/`, `firebase/database.rules.json`
- новые `data/favorites/`, `reminders/FavoriteAnniversaryWorker.kt`, `ui/details/ui/DetailsScreen.kt`
- `ui/tours/…` (прогресс), `ui/home/ui/HomeScreen.kt` (QR), `gradle/libs.versions.toml`, `CLAUDE.md`

## Проверка

1. После каждого шага: `./gradlew assembleDebug detektAll testDebugUnitTest`.
2. Тесты: `SettingsRepository` (значения по умолчанию, сохранение), `TextScale`, разбор QR-ссылки, `anniversaryOn`, `FavoritesRepository.toggle`, прогресс экскурсии («продолжить» выбирает первую непройденную).
3. Вручную на устройстве:
   - тёмная тема: переключение в «Ещё» и по системной настройке, карта ночью, иконки статус-бара читаются на всех экранах-героях;
   - «Очень крупный» текст не ломает карточки и бар;
   - язык сохраняется после перезапуска приложения;
   - бар плавает над списком, последний элемент списка не прячется под бар, кнопки карты не перекрыты;
   - отправить сообщение → ответить из админки → ответ и значок видны в «Мои обращения»;
   - добавить ветерана в избранное с датой рождения «сегодня» → запустить Worker (`adb shell cmd jobscheduler run`) → уведомление открывает карточку;
   - экскурсия: отметить 2 остановки, выйти, вернуться → «2 из 4», выбрана третья;
   - QR: скан таблички открывает карточку, чужой QR показывает сообщение.
4. Слить обновлённые правила в консоли Firebase вручную.
