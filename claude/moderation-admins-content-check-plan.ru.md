# Отклонение заявок, инструкция по администраторам, строже проверка контента

## Контекст

1. **Заявка не отклоняется.** При нажатии «Отклонить» появляется «Не удалось сохранить. Проверьте подключение к интернету», хотя интернет есть. `SubmissionReviewViewModel.runDecision` показывает этот текст на **любую** ошибку записи, поэтому настоящую причину видно только в logcat. Сам код `reject()` корректен. Скорее всего, запись отклоняют правила базы: `.validate` у `Submissions/$submissionId` проверяет `hasChildren([... 'contact' ...])` и `id === $submissionId` **и для админа тоже**. Если заявка лежит в базе в старом формате (без `id`/`contact`) или в консоли опубликованы не те правила, что в репозитории, Firebase вернёт `PERMISSION_DENIED`. Устройство сейчас не подключено, и прочитать лог я не могу: гипотезу подтверждаю на шаге 1.1.
2. **Как добавить админа.** Инструкция есть только в CLAUDE.md. Её нужно показать в приложении.
3. **Проверка на непристойное содержание.** `ProfanityDetector` пропускает маскировку (`х*й`, `бл@ть`), словоформы (`суками`, `сукин`), английский мат, оскорбления без мата и нацистскую символику, а для воинского мемориала она важнее всего. Пороги NSFW мягкие, а класс `sexy` сам по себе не блокируется.

Здесь моё мнение расходится с вашим выбором. Вы выбрали инструкцию «на экране „О приложении“», но вкладку «О приложении» видят все посетители, а рассказывать им, как получить права администратора, незачем. Предлагаю новый раздел во **внутреннем руководстве редактора** (`ui/admin/guide`). Его открывают из вкладки «Админ», и он уже разбит на разделы. Если нужен именно экран «О приложении», скажите: перенесу.

---

## 1. Отклонение заявки

### 1.1 Сначала диагностика

Подключаю телефон, выполняю `adb logcat -s SubmissionReviewViewModel:E` и жму «Отклонить». Если в логе `Permission denied`, верны шаги 1.2–1.3. Если там другая ошибка, чиню её и сообщаю вам, прежде чем идти дальше.

### 1.2 Правила: админ не зависит от формата старых заявок

**Почему:** проверка формата нужна, только чтобы посетитель не записал мусор. Если админ правит существующую запись, а у неё нет `contact` или `id`, `updateChildren` получает отказ, и отклонить такую заявку нельзя. То же самое с `Feedback`.

```json
"$submissionId": {
  ".write": "auth != null && (!data.exists() || root.child('OurMemory/Admins/' + auth.uid).exists())",
  ".validate": "root.child('OurMemory/Admins/' + auth.uid).exists() || (newData.hasChildren(['id', 'veteranId', 'text', 'contact', 'status', 'createdAt']) && newData.child('id').val() === $submissionId && newData.child('authorUid').val() === auth.uid && newData.child('status').val() === 'pending' && newData.child('text').isString() && newData.child('text').val().length <= 5000 && newData.child('createdAt').val() === now)"
}
```

`firebase/database.rules.json` не подключён к `firebase.json`, поэтому правила **нужно вручную перенести в консоль** в узел `OurMemory`. Кроме того, надо сверить, что в консоли стоит актуальная версия правил, а не старая.

### 1.3 Показывать настоящую причину, а не «нет интернета»

**Почему:** если нет прав, сообщение «проверьте интернет» уводит админа не туда. Код ошибки Firebase нужно донести до экрана.

`data/moderation/datasource/remote/ModerationRemoteDataSourceImpl.kt`: вместо `.await()` вызываю `updateChildren` с `CompletionListener` и перевожу `PERMISSION_DENIED` в доменное исключение:

```kotlin
private suspend fun DatabaseReference.updateOrThrow(updates: Map<String, Any>) =
    suspendCancellableCoroutine { continuation ->
        updateChildren(updates) { error, _ ->
            when {
                error == null -> continuation.resume(Unit)
                error.code == DatabaseError.PERMISSION_DENIED -> continuation.resumeWithException(ModerationDeniedException())
                else -> continuation.resumeWithException(error.toException())
            }
        }
    }
```

`domain/models/ModerationDeniedException.kt`:

```kotlin
class ModerationDeniedException : IllegalStateException()
```

`ui/admin/moderation/models/ReviewFailure.kt`:

```kotlin
enum class ReviewFailure(@param:StringRes val messageRes: Int) {
    NETWORK(R.string.failed_to_save_msg),
    NO_PERMISSION(R.string.no_permission_to_change_request_msg)
}
```

В `SubmissionReviewViewModel` поле `ReviewForm.hasFailed: Boolean` заменяю на `failure: ReviewFailure?`:

```kotlin
.onFailure { error ->
    Log.e(LOG_TAG, "Failed to moderate submission $submissionId", error)
    val failure = if (error is ModerationDeniedException) ReviewFailure.NO_PERMISSION else ReviewFailure.NETWORK
    form.update { it.copy(isProcessing = false, failure = failure) }
}
```

`SubmissionReviewUiState.Success.hasFailed` становится `failure: ReviewFailure?`, а экран выводит `stringResource(failure.messageRes)`. Новая строка:

```xml
<string name="no_permission_to_change_request_msg">Нет прав на изменение заявки. Проверьте, что ваш UID есть в OurMemory/Admins и правила базы опубликованы</string>
```

(в `values-be` добавляю перевод). Существующие тесты `SubmissionReviewViewModelTest` правлю под `failure` и добавляю тест: `ModerationDeniedException` → `ReviewFailure.NO_PERMISSION`.

---

## 2. Инструкция «Как добавить администратора»

**Почему:** сейчас порядок действий знает только разработчик. Шагов три, и они в разных местах (Authentication, Database, Storage rules). Если пропустить Storage, новый админ не загрузит фото.

`ui/admin/guide/models/GuideSection.kt`: добавляю раздел перед `TROUBLESHOOTING`:

```kotlin
ADMINISTRATORS(
    R.string.administrators,
    R.drawable.shield,
    R.array.add_administrator_steps,
    R.string.do_not_publish_whole_rules_file_msg
),
```

`values/strings.xml` (и `values-be/strings.xml`):

```xml
<string name="administrators">Администраторы</string>
<string name="do_not_publish_whole_rules_file_msg">Не публикуйте файлы правил из проекта целиком: база общая с другими приложениями, и их правила сотрутся. Правьте только узел OurMemory</string>
<string-array name="add_administrator_steps">
    <item>Firebase Console → Authentication → Users → «Add user»: укажите e-mail и пароль нового администратора.</item>
    <item>Скопируйте из списка пользователей его User UID.</item>
    <item>Realtime Database → OurMemory → Admins: добавьте ключ с этим UID и значением true.</item>
    <item>Storage → Rules: допишите UID в список функции isAdmin(), например [\'uid1\', \'uid2\'], и нажмите «Publish».</item>
    <item>Новый администратор входит на вкладке «О приложении» → «Вход для администратора» — после этого у него появится вкладка «Админ».</item>
    <item>Чтобы снять права, удалите ключ из OurMemory/Admins и UID из правил Storage.</item>
</string-array>
```

Ещё поправлю пункт `troubleshooting_steps` про «Нет прав администратора», чтобы он ссылался на новый раздел.

---

## 3. Проверка текста

### 3.1 `MaskedWords`: раскрытие маскировки

**Почему:** сейчас `NON_LETTERS` режет `х*й` на `х` и `й`. Одиночные буквы склеиваются в `хй`, и корень не находится. Маскировка нужна и кириллическому, и латинскому детектору, поэтому она вынесена в отдельный объект.

Каждый токен с символами маски раскрывается во все варианты, где маска заменена пустой строкой или одной буквой нужного алфавита. Если масок больше двух, они просто вырезаются, чтобы число вариантов не росло экспоненциально.

`data/contentcheck/datasource/local/MaskedWords.kt`:

```kotlin
object MaskedWords {

    private const val MAX_MASK_RUNS = 2
    private val MASK_RUN = Regex("[*#%$@_.\\-]+")
    private val TOKEN_SEPARATORS = Regex("[\\s,!?;:()\"«»]+")
    private val EDGE_PUNCTUATION = charArrayOf('.', '-', '_')

    fun expand(text: String, letters: String): List<String> = text.lowercase()
        .split(TOKEN_SEPARATORS)
        .map { token -> token.trim { it in EDGE_PUNCTUATION } }
        .filter { token -> MASK_RUN.containsMatchIn(token) && token.any(Char::isLetter) }
        .flatMap { token -> expandToken(token, letters) }

    private fun expandToken(token: String, letters: String): List<String> {
        val parts = token.split(MASK_RUN)
        if (parts.size - 1 > MAX_MASK_RUNS) return listOf(parts.joinToString(""))
        val replacements = listOf("") + letters.map(Char::toString)
        return parts.drop(1).fold(listOf(parts.first())) { prefixes, part ->
            prefixes.flatMap { prefix -> replacements.map { prefix + it + part } }
        }
    }
}
```

### 3.2 `ProfanityDetector`: словоформы, двойники, оскорбления

**Почему:** `сука` проверяется только целым словом, поэтому `суками` и `сукин` проходят. Латинские `u`, `n`, `r`, `i` и цифра `4` не приводятся к кириллице. Оскорбления без мата (`мразь`, `ублюдок`, `гнида`) не ловятся вовсе. Отдельный детектор для них не нужен: они проверяются так же, как мат, по корням и целым словам.

Латинская `u` похожа и на `и`, и на `у`, поэтому текст проверяется с двумя таблицами двойников. Проверка префиксом (`startsWith`) не даёт ложных срабатываний на `барсук` и `сукно`.

```kotlin
fun containsProfanity(text: String) =
    (listOf(text) + MaskedWords.expand(text, CYRILLIC_LETTERS)).any { candidate ->
        LOOKALIKE_VARIANTS.any { lookalikes -> normalizedWords(candidate, lookalikes).any(::isProfane) }
    }

private fun isProfane(word: String): Boolean {
    val cleaned = SAFE_FRAGMENTS.fold(word) { current, safe -> current.replace(safe, "") }
    return cleaned in EXACT_WORDS ||
        PREFIX_ROOTS.any { cleaned.startsWith(it) } ||
        INFIX_ROOTS.any { it in cleaned }
}
```

```kotlin
private const val CYRILLIC_LETTERS = "абвгдежзийклмнопрстуфхцчшщъыьэюя"
private val LOOKALIKE_VARIANTS = listOf(LOOKALIKES, LOOKALIKES + ('u' to 'у'))
private val PREFIX_ROOTS = listOf(
    "сука", "суки", "сукам", "сукин", "сучар", "сучий", "сучье", "падл", "шалав", "мразот"
)
```

В `LOOKALIKES` добавлены `'u' to 'и', 'n' to 'п', 'r' to 'г', 'i' to 'и', 'і' to 'и', '4' to 'ч'`, а `'@'` убран: теперь это символ маски. В `INFIX_ROOTS` добавлены `ебац` (белорусская форма) и `ублюд`. В `EXACT_WORDS` добавлены `бляць`, `мразь`, `тварь`, `чмо`, `урод`, `дебил`, `выродок`, `гнида` с формами множественного числа.

### 3.3 Латиница: `LatinProfanityDetector`

**Почему:** `ProfanityDetector` переводит латиницу в кириллицу, поэтому `fuck` не совпадает ни с чем, а транслит `blyat` и `suka` проходит. Для латиницы нужна обратная таблица двойников (кириллица → латиница) и свой словарь, куда входит и английский мат, и русский мат транслитом.

`data/contentcheck/datasource/local/LatinProfanityDetector.kt`:

```kotlin
class LatinProfanityDetector @Inject constructor() {

    fun containsProfanity(text: String) =
        (listOf(text) + MaskedWords.expand(text, LATIN_LETTERS)).any { candidate ->
            normalizedWords(candidate).any { word -> word in EXACT_WORDS || INFIX_ROOTS.any { it in word } }
        }

    private fun normalizedWords(text: String): List<String> {
        val words = text.lowercase()
            .map { LOOKALIKES[it] ?: it }
            .joinToString("")
            .split(NON_LETTERS)
            .filter { it.isNotEmpty() }
        return words + words.map { it.replace(REPEATED_LETTERS, "$1") }
    }

    companion object {
        private const val LATIN_LETTERS = "abcdefghijklmnopqrstuvwxyz"
        private val REPEATED_LETTERS = Regex("(\\p{L})\\1+")
        private val NON_LETTERS = Regex("[^\\p{L}]+")
        private val LOOKALIKES = mapOf(
            'а' to 'a', 'о' to 'o', 'е' to 'e', 'р' to 'p', 'с' to 'c', 'х' to 'x', 'у' to 'y',
            'к' to 'k', 'м' to 'm', 'т' to 't', 'і' to 'i',
            '0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', '!' to 'i', '$' to 's'
        )
        private val INFIX_ROOTS = listOf(
            "fuck", "bitch", "whore", "nigger", "nigga", "asshole", "motherf", "bullshit",
            "pizd", "pezd", "blyad", "blyat", "bliat", "xuy", "xuj", "huyn", "nahuy", "nahui",
            "ebal", "eban", "ebat", "zaeb", "naeb", "pidor", "pidar", "mudak"
        )
        private val EXACT_WORDS = setOf(
            "shit", "shitty", "cunt", "cunts", "dick", "slut", "fag", "faggot", "fck", "fuk", "bastard",
            "huy", "huj", "blya", "suka", "suki"
        )
    }
}
```

Слова вида `shit`, `dick` и `cunt` проверяются целиком, а не по корню, иначе сработали бы `shitake`, `Dickens` и `Scunthorpe`. Удвоенные буквы проверяются в обоих вариантах: сжатие ломает `asshole` и `bullshit`, а без сжатия проходит `fuuuck`.

### 3.4 Нацистская символика: `ExtremismDetector`

**Почему:** на воинском мемориале это самый вредный вид контента. Слова `Гитлер`, `фашисты`, `СС` законно встречаются в биографиях («воевал против фашистов»), поэтому блокируются только фразы прославления, числовые коды и символы, а не отдельные слова. Фразы сравниваются целыми словами, чтобы `зига` не находилась внутри других слов.

`data/contentcheck/datasource/local/ExtremismDetector.kt`:

```kotlin
class ExtremismDetector @Inject constructor() {

    fun containsExtremism(text: String): Boolean {
        val words = text.lowercase().replace('ё', 'е').split(SEPARATORS).filter { it.isNotEmpty() }
        val normalized = words.joinToString(separator = " ", prefix = " ", postfix = " ")
        return SYMBOLS.any { it in text } ||
            NUMERIC_CODES.containsMatchIn(text) ||
            PHRASES.any { " $it " in normalized }
    }

    companion object {
        private val SEPARATORS = Regex("[^\\p{L}\\p{N}]+")
        private val NUMERIC_CODES = Regex("(?<!\\d)14\\s*[/\\\\-]?\\s*88(?!\\d)")
        private val SYMBOLS = listOf("卐", "卍", "ϟϟ", "ᛋᛋ", "ᛊᛊ")
        private val PHRASES = listOf(
            "хайль", "зиг хайль", "хайль гитлер", "слава гитлеру", "гитлер прав", "гитлер был прав",
            "зига", "зигу", "зиговать", "зигует", "зигуют",
            "heil", "sieg heil", "heil hitler", "white power", "white pride"
        )
    }
}
```

### 3.5 Объединение в репозитории

**Почему:** view model не должна знать, сколько детекторов стоит внутри. После расширения название `containsProfanity` стало бы неверным.

`domain/repository/ContentCheckRepository.kt`: `containsProfanity` → `containsOffensiveText`.

```kotlin
override fun containsOffensiveText(text: String) =
    profanityDetector.containsProfanity(text) ||
        latinProfanityDetector.containsProfanity(text) ||
        extremismDetector.containsExtremism(text)
```

`ContentCheckModule` передаёт в репозиторий два новых детектора:

```kotlin
): ContentCheckRepository = ContentCheckRepositoryImpl(
    profanityDetector = profanityDetector,
    latinProfanityDetector = latinProfanityDetector,
    extremismDetector = extremismDetector,
    imageClassifier = imageClassifier,
    ioDispatcher = ioDispatcher
)
```

Вызовы в `SubmissionViewModel`, `FeedbackViewModel` и `FakeContentCheckRepository` переименованы. Текст `remove_offensive_words_msg` изменён на «Уберите нецензурные и оскорбительные выражения — такое сообщение не будет отправлено» (ru и be).

---

## 4. Проверка фото

**Почему:** при порогах 0.6 и 0.8 откровенное фото в белье (`sexy` ≈ 0.7, `porn` ≈ 0.2) проходит. На мемориальном сайте такому фото не место, а ложное срабатывание обходится дёшево: родственник выберет другое фото, и модерация всё равно остаётся.

`ContentCheckRepositoryImpl`:

```kotlin
private fun NsfwScores.isExplicit() =
    porn >= EXPLICIT_THRESHOLD ||
        hentai >= EXPLICIT_THRESHOLD ||
        sexy >= SEXY_THRESHOLD ||
        porn + hentai + sexy >= COMBINED_THRESHOLD

companion object {
    private const val EXPLICIT_THRESHOLD = 0.4f
    private const val SEXY_THRESHOLD = 0.6f
    private const val COMBINED_THRESHOLD = 0.6f
}
```

Нечитаемые фото (`UNREADABLE`) уже блокируются через `could_not_open_photo_msg`, их не трогаю.

---

## 5. Тесты

`ProfanityDetector`, `LatinProfanityDetector` и `ExtremismDetector` написаны на чистом Kotlin, поэтому для них подходят обычные JUnit-тесты в `app/src/test/.../data/contentcheck/datasource/local/`. На каждый детектор есть тест, что он находит нужное, и тест на ложные срабатывания:

```kotlin
@Test
fun detectsMaskedWords() {
    listOf("х*й", "бл@ть", "п.зда", "бл*ть", "с_у_к_а", "сук@", "е#ать", "н@х")
        .forEach { assertTrue(it, detector.containsProfanity(it)) }
}

@Test
fun detectsLookalikeSpelling() {
    listOf("xуй", "хuй", "пuзда", "cукa", "мpaзь", "6ля", "с у к а")
        .forEach { assertTrue(it, detector.containsProfanity(it)) }
}
```

```kotlin
@Test
fun detectsNaziSlogansCodesAndSymbols() {
    listOf("Зиг хайль!", "Sieg Heil", "хайль, гитлер", "1488", "14/88", "14 88", "卐", "white power")
        .forEach { assertTrue(it, detector.containsExtremism(it)) }
}
```

Среди текстов, которые должны пройти: `"Барсук"`, `"сукно"`, `"страхуй"`, `"Scunthorpe"`, `"Dickens"`, `"shitake"`, `"Гитлер напал на СССР 22 июня 1941 года"`, `"родился в 1914, умер в 1988"`.

`SubmissionReviewViewModelTest.deniedRejectionShowsPermissionError` проверяет, что `ModerationDeniedException` превращается в `ReviewFailure.NO_PERMISSION`, а не в `NETWORK`.

## Проверка

```bash
./gradlew testDebugUnitTest
./gradlew detektAll
./gradlew assembleDebug
```

Вручную:
1. Перенести правила из шага 1.2 в консоль. Админ открывает заявку и жмёт «Отклонить»: экран закрывается, в списке статус «Отклонено», у посетителя в «Мои обращения» заявка отклонена, ответ виден.
2. Временно убрать свой UID из `Admins` и нажать «Отклонить»: показывается «Нет прав на изменение заявки…», а не «проверьте интернет». Затем вернуть UID.
3. Админ → Руководство → «Администраторы»: шесть шагов на русском и на белорусском (переключение языка на «О приложении»).
4. Форма материалов и обратная связь: `х*й`, `суками`, `fuck`, `мразь`, `Зиг хайль`, `14/88` дают ошибку отправки, а «Воевал против фашистов» отправляется.
5. Прикрепить фото в купальнике или белье: «Фото не прошло автоматическую проверку». Обычный портрет проходит.
