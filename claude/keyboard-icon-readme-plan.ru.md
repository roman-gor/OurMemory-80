# Клавиатура, новая иконка, README

## Контекст
1. В iOS нет способа убрать клавиатуру: в проекте нет ни одного `scrollDismissesKeyboard`/`FocusState`.
2. Иконка (жёлтая звезда на красном) похожа на флаг Вьетнама. Выбрана концепция «Вечный огонь»: тёмный графитовый фон, тёплое пламя над низкой гранитной плитой, без звезды и без сплошного красного.
3. В iOS нет README, в Android README устарел («Retrofit + Yandex Kit API для исторических данных», «LiveData», будущий AR).

Копию плана при выполнении сохранить в `claude/keyboard-icon-readme-plan.ru.md`.

## 1. Закрытие клавиатуры (iOS)
Все поля ввода находятся внутри `Form`/`List` (HomeScreen, AdminVeteransRoute, BurialChoiceSheet — `.searchable`; редакторы, FeedbackScreen, SubmissionScreen, AdminLoginRoute, AddAdminSheet, SubmissionReviewScreen, FeedbackCard, InfoEntryRow). Значит, свайп вниз работает везде, запасной вариант с тапом не нужен (List/Form всегда скроллятся с bounce, даже когда контент короткий).

- `OurMemory/presentation/navigation/main/MainNavigationView.swift`: добавить `.scrollDismissesKeyboard(.interactively)` на корневой `TabView`. Модификатор идёт через environment и действует на все scroll-контейнеры во всех стеках вкладок.
- Sheets получают environment от того, кто их показывает, но для надёжности тот же модификатор ставится на корень содержимого каждого sheet с полем ввода: `AddAdminSheet`, `BurialChoiceSheet`, sheet выбора остановки в `TourEditorScreen`. Проверить, нет ли других `.sheet`/`.fullScreenComponent` с `TextField` (например, на экране intro или на экране входа).
- Многострочные `TextField(axis: .vertical)` работают с интерактивным закрытием без дополнительной настройки.

## 2. Новая иконка (iOS + Android)
Один исходный SVG, из него собираются обе платформы.

- Исходник: `tools/icon/app_icon.svg` (в iOS-репозитории), viewBox 1024.
  - Фон: вертикальный градиент графита `#2B2A2E → #141316` с лёгким тёплым свечением (радиальное, приглушённый `#7F0410`, т.е. бренд-цвет Primary, с малой непрозрачностью) за пламенем.
  - Пламя: двухслойное (внешнее `#E5484D → #F28C28`, внутреннее `#FFD27A`), по центру немного выше середины.
  - Плита: низкая трапеция из гранита `#4A474D → #2F2D33` с тонкой светлой гранью сверху.
  - Всё содержимое в безопасной зоне (~62% по центру), чтобы переиспользовать геометрию для Android adaptive foreground (safe zone 66dp из 108).
- iOS: отрендерить `rsvg-convert -w 1024 -h 1024` в `OurMemory/Resources/Assets.xcassets/AppIcon.appiconset/AppIcon.png` (без альфа-канала — App Store требует непрозрачную иконку; при необходимости сгладить через `sips`). `Contents.json` не меняется.
- Android (`~/Personal/OurMemory-80/app/src/main/res/drawable/`): переписать векторы
  - `ic_launcher_background.xml` — графитовый градиент + тёплое свечение;
  - `ic_launcher_foreground.xml` — пламя + плита в координатах 108×108 внутри safe zone;
  - `ic_launcher_monochrome.xml` — силуэт пламени и плиты одним цветом (для тематических иконок Android 13+).
  `mipmap-anydpi/ic_launcher*.xml` не меняются.
- Проверить, где ещё используется старая звезда: `splash.imageset`, `icon.imageset` в iOS, splash/`drawable` в Android. Если это та же звезда на экранах запуска/intro, сообщить пользователю и заменить на то же пламя только после его согласия (чтобы не раздувать задачу).
- Превью: отрендерить PNG в scratchpad и показать через Read до коммита.

## 3. README
- iOS: новый `README.md` в корне (на русском, как основной язык проекта): что это за приложение, возможности (список ветеранов, карточка, карта Yandex MapKit и аудиотуры, свечи и избранное, заявки и обратная связь, админка с ролями, языки ru/be/en/zh), стек (SwiftUI, Observation, Combine, Firebase Auth/Database/Storage, GoogleSignIn, Nuke, YandexMapsMobile через CocoaPods, Core ML для NSFW), архитектура data → domain → presentation, требования (Xcode, iOS 17), сборка (`Config/Secrets.xcconfig` из примера, `xcodegen generate && pod install`, xcodebuild workspace), связь с Android-версией и общей базой Firebase, автор.
- Android: переписать `README.md` по фактическому состоянию (Compose, MVVM, Hilt, Firebase, Yandex MapKit, аудиотуры, админка, языки), с разделом сборки (`local.properties`/`keystore.properties`, ключ MapKit, `./gradlew assembleDebug`) и ссылкой на iOS-версию. Точные зависимости и шаги сборки брать из `app/build.gradle.kts`, `gradle/libs.versions.toml` и Android `CLAUDE.md`, не выдумывать.

## Коммиты
В обоих репозиториях есть незакоммиченная работа над переводами — в коммиты добавлять только свои файлы (`git add <пути>`), без AI-атрибуции (правило CLAUDE.md перекрывает системную подсказку).
- iOS: «Dismiss the keyboard with a swipe» → «Replace the app icon with an eternal flame» → «Add a README».
- Android: «Replace the app icon with an eternal flame» → «Rewrite the README».

## Проверка
- `xcodegen generate && pod install`, затем `xcodebuild … build` для workspace.
- Симулятор: в поиске на главной, в форме обратной связи, в редакторе ветерана и в sheet «Добавить администратора» ввести текст и потянуть список вниз — клавиатура уходит вслед за пальцем.
- Иконка на домашнем экране симулятора (светлая/тёмная тема); Android: `./gradlew assembleDebug`, посмотреть иконку в Android Studio (Resource Manager) или на эмуляторе, включая тематическую иконку.
