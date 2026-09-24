# Предварительная проверка обращений: мат в тексте и непристойные фото

## Контекст

Посетители отправляют два вида обращений:
- **материалы родственников** (`SubmissionScreen`): текст, контакт и до 5 фото, которые загружаются в Storage;
- **обратную связь** (`FeedbackScreen`): текст и контакт.

Сейчас всё уходит в базу без проверки, и администраторы видят всё, что прислали, включая мат и непристойные фото. Нужен автоматический фильтр **до** отправки, чтобы такое содержимое не попадало ни в базу, ни в Storage, ни к администраторам.

Что решил пользователь:
1. **Фото** проверяются на телефоне встроенной моделью NSFW-классификатора (TFLite).
2. **При непройденной проверке** ничего не отправляется. Плохое фото сразу убирается из выбранных с пояснением. Если в тексте или контакте есть мат, отправка не происходит, а поле подсвечивается.

### Ограничение, о котором надо знать
Проверка работает на клиенте, поэтому изменённое приложение или прямой запрос к REST API Firebase её обойдут. Правила Realtime Database не умеют надёжно искать мат (регулярные выражения там ограничены), а проверка фото на сервере требует Cloud Functions и тарифа Blaze. Для обычных посетителей клиентского фильтра достаточно. Серверную проверку можно добавить позже, если проект переедет на Blaze.

После одобрения я первым делом скопирую этот план в `claude/content-precheck-plan.ru.md`.

---

## 1. Доменный контракт

### Почему
Формы (`SubmissionViewModel`, `FeedbackViewModel`) не должны знать, как устроены фильтр слов и модель. Им нужен один репозиторий с двумя вопросами: есть ли в тексте мат и можно ли отправлять фото.

`domain/models/PhotoCheckResult.kt`
```kotlin
enum class PhotoCheckResult {
    ALLOWED,
    BLOCKED,
    UNREADABLE
}
```

`domain/repository/ContentCheckRepository.kt`
```kotlin
interface ContentCheckRepository {
    fun containsProfanity(text: String): Boolean
    suspend fun checkPhoto(uri: String): PhotoCheckResult
}
```

`UNREADABLE` означает, что фото не удалось декодировать. Такое фото тоже не принимаем, но с другим сообщением.

---

## 2. Фильтр мата

### Почему
Мат пишут с обходами: «х у й», «хуууй», латиница вместо кириллицы («xyй», «пи3да»), «ё» вместо «е». Если искать по простому списку слов, почти всё проскочит. Если искать по корням без исключений, будут ложные срабатывания («хлебало» содержит «ебал», «застрахуй» содержит «хуй», «хулиган» начинается с «хули»).

Поэтому фильтр работает в три шага:
1. **Нормализация.** Нижний регистр, «ё» → «е», латинские буквы и цифры, похожие на кириллические, заменяются кириллицей (`a o e p c x y k m 3 0 6 @`). Повторяющиеся буквы схлопываются, а одиночные буквы через пробелы или точки склеиваются: «х.у.й» → «хуй».
2. **Белый список.** Из каждого слова вырезаются безопасные фрагменты, содержащие запрещённые корни: «страху», «хлеб», «хулиган», «команд», «мандарин».
3. **Поиск.** Корни ищутся внутри слова (INFIX: «хуй», «пизд», «ебан», «бляд»…), а короткие слова, у которых есть безобидные родственники, сравниваются целиком (EXACT: «бля», «сука», «хули», «манда», «курва»…).

### Код
`data/contentcheck/datasource/local/ProfanityDetector.kt`
```kotlin
class ProfanityDetector @Inject constructor() {

    fun containsProfanity(text: String): Boolean = normalizedWords(text).any { word ->
        val cleaned = SAFE_FRAGMENTS.fold(word) { current, safe -> current.replace(safe, "") }
        cleaned in EXACT_WORDS || INFIX_ROOTS.any { it in cleaned }
    }

    private fun normalizedWords(text: String): List<String> {
        val normalized = text.lowercase()
            .map { LOOKALIKES[it] ?: it }
            .joinToString("")
            .replace(REPEATED_LETTERS, "$1")
        val words = normalized.split(NON_LETTERS).filter { it.isNotEmpty() }
        return words + joinSingleLetters(words)
    }

    private fun joinSingleLetters(words: List<String>): List<String> = words
        .fold(mutableListOf(StringBuilder())) { groups, word ->
            if (word.length == 1) groups.last().append(word) else groups.add(StringBuilder())
            groups
        }
        .map { it.toString() }
        .filter { it.length >= MIN_JOINED_LENGTH }

    companion object {
        private const val MIN_JOINED_LENGTH = 3
        private val REPEATED_LETTERS = Regex("(\\p{L})\\1+")
        private val NON_LETTERS = Regex("[^\\p{L}]+")
        private val LOOKALIKES = mapOf(
            'a' to 'а', 'o' to 'о', 'e' to 'е', 'p' to 'р', 'c' to 'с', 'x' to 'х', 'y' to 'у',
            'k' to 'к', 'm' to 'м', 'b' to 'в', 'h' to 'н', 't' to 'т', 'ё' to 'е',
            '3' to 'з', '0' to 'о', '6' to 'б', '@' to 'а'
        )
        private val SAFE_FRAGMENTS = listOf("страху", "хлеб", "хулиган", "команд", "мандарин", "мандат")
        private val INFIX_ROOTS = listOf(
            "хуй", "хуе", "хуя", "хуи", "пизд", "пезд", "бляд", "блят", "ебан", "ебал", "ебат", "ебуч",
            "ебну", "еблан", "ебло", "заеб", "наеб", "выеб", "уеб", "поеб", "съеб", "сьеб", "залуп",
            "мудак", "мудил", "мудозвон", "гандон", "пидор", "пидар", "пидр", "шлюх", "херн", "долбоеб"
        )
        private val EXACT_WORDS = setOf(
            "бля", "сука", "суки", "сучка", "сучара", "хули", "хер", "манда", "курва", "пох", "нах"
        )
    }
}
```
Слова, которые чаще всего ломают такие фильтры («хлебать», «застраховать», «хулиган», «мандарин», «себе», «небо»), разберу вручную по чек-листу из раздела «Проверка». По ходу списки корней и безопасных фрагментов можно пополнять.

---

## 3. Проверка фото моделью TFLite

### Почему
Нужно отсечь порнографию и откровенные снимки ещё до загрузки в Storage, без сервера и оплаты. Подходит открытая модель **GantMan/nsfw_model** (MobileNet V2, вход 224×224, лицензия MIT). Она выдаёт вероятности пяти классов: `drawings, hentai, neutral, porn, sexy`. Архивные фото военных лет модель относит к `neutral` или `drawings`, так что ложные блокировки маловероятны.

### 3.1 Модель
- `tools/nsfw/convert_model.py` и `tools/nsfw/requirements.txt`: скачивают Keras-модель `mobilenet_v2_140_224` из релиза GantMan и конвертируют её в `app/src/main/assets/nsfw_mobilenet_v2_224.tflite` с float16-квантованием (~3–5 МБ). Скрипт запускается один раз, готовый `.tflite` коммитится.
- TensorFlow пока не поддерживает системный Python 3.14. Скрипт запускается на Python 3.11:
  ```bash
  uv run --python 3.11 --with-requirements tools/nsfw/requirements.txt tools/nsfw/convert_model.py
  ```
- Рядом с моделью кладётся `app/src/main/assets/nsfw_model_LICENSE.txt` (MIT, авторство GantMan).

### 3.2 Зависимость
`gradle/libs.versions.toml`
```toml
[versions]
litert = "1.4.0"

[libraries]
litert = { group = "com.google.ai.edge.litert", name = "litert", version.ref = "litert" }
```
Актуальную версию LiteRT сверю при реализации. `app/build.gradle.kts` получает `androidResources { noCompress += "tflite" }`, чтобы модель можно было отобразить в память напрямую из APK.

### 3.3 Классификатор
`data/contentcheck/datasource/local/NsfwImageClassifier.kt`
```kotlin
@Singleton
class NsfwImageClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val interpreter by lazy { Interpreter(loadModel(), Interpreter.Options().setNumThreads(THREADS)) }
    private val mutex = Mutex()

    suspend fun classify(uri: Uri): NsfwScores = mutex.withLock {
        val input = toInputBuffer(decode(uri))
        val output = Array(1) { FloatArray(CLASS_COUNT) }
        interpreter.run(input, output)
        NsfwScores(
            hentai = output[0][HENTAI_INDEX],
            porn = output[0][PORN_INDEX],
            sexy = output[0][SEXY_INDEX]
        )
    }

    private fun decode(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.setTargetSize(INPUT_SIZE, INPUT_SIZE)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun toInputBuffer(bitmap: Bitmap): ByteBuffer {
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        bitmap.recycle()
        return ByteBuffer.allocateDirect(pixels.size * CHANNELS * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .apply {
                pixels.forEach { pixel ->
                    putFloat(Color.red(pixel) / MAX_CHANNEL_VALUE)
                    putFloat(Color.green(pixel) / MAX_CHANNEL_VALUE)
                    putFloat(Color.blue(pixel) / MAX_CHANNEL_VALUE)
                }
                rewind()
            }
    }

    private fun loadModel(): MappedByteBuffer = context.assets.openFd(MODEL_FILE).use { descriptor ->
        FileInputStream(descriptor.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY,
            descriptor.startOffset,
            descriptor.declaredLength
        )
    }

    companion object {
        private const val MODEL_FILE = "nsfw_mobilenet_v2_224.tflite"
        private const val INPUT_SIZE = 224
        private const val CHANNELS = 3
        private const val CLASS_COUNT = 5
        private const val HENTAI_INDEX = 1
        private const val PORN_INDEX = 3
        private const val SEXY_INDEX = 4
        private const val THREADS = 2
        private const val MAX_CHANNEL_VALUE = 255f
    }
}
```
`data/contentcheck/model/NsfwScores.kt`
```kotlin
data class NsfwScores(
    val hentai: Float = 0f,
    val porn: Float = 0f,
    val sexy: Float = 0f
)
```

### 3.4 Репозиторий и пороги
`data/contentcheck/repository/ContentCheckRepositoryImpl.kt`
```kotlin
class ContentCheckRepositoryImpl @Inject constructor(
    private val profanityDetector: ProfanityDetector,
    private val imageClassifier: NsfwImageClassifier,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentCheckRepository {

    override fun containsProfanity(text: String) = profanityDetector.containsProfanity(text)

    override suspend fun checkPhoto(uri: String): PhotoCheckResult = withContext(ioDispatcher) {
        runCatching { imageClassifier.classify(uri.toUri()) }
            .map { scores -> if (scores.isExplicit()) PhotoCheckResult.BLOCKED else PhotoCheckResult.ALLOWED }
            .getOrDefault(PhotoCheckResult.UNREADABLE)
    }

    private fun NsfwScores.isExplicit() =
        porn >= EXPLICIT_THRESHOLD || hentai >= EXPLICIT_THRESHOLD || porn + hentai + sexy >= COMBINED_THRESHOLD

    companion object {
        private const val EXPLICIT_THRESHOLD = 0.6f
        private const val COMBINED_THRESHOLD = 0.8f
    }
}
```
Модель подключается в новом `di/ContentCheckModule.kt` (`@Provides @Singleton`), так же как в `AccountModule`.

---

## 4. Форма материалов родственников

### Почему
Фото проверяются сразу после выбора: так посетитель видит результат до того, как заполнит всё остальное, а плохое фото не хранится даже в состоянии формы. Текст и контакт проверяются при нажатии «Отправить».

`ui/submission/models/SubmissionUiState.kt`
```kotlin
data class SubmissionUiState(
    val veteranName: String = "",
    val text: String = "",
    val contact: String = "",
    val photoUris: ImmutableList<String> = persistentListOf(),
    val checkingPhotosCount: Int = 0,
    val photoRejection: PhotoCheckResult? = null,
    val hasProfanity: Boolean = false,
    val hasConsent: Boolean = false,
    val status: SubmissionStatus = SubmissionStatus.EDITING
) {
    val canSend = text.isNotBlank() && contact.isNotBlank() && hasConsent &&
        checkingPhotosCount == 0 && status != SubmissionStatus.SENDING
    val canAddPhotos = photoUris.size + checkingPhotosCount < MAX_PHOTOS && status != SubmissionStatus.SENDING

    companion object {
        const val MAX_PHOTOS = 5
    }
}
```

`SubmissionViewModel`: выбор фото, изменение текста и отправка.
```kotlin
is SubmissionUiIntent.OnPhotosPicked -> checkPhotos(intent.uris)
is SubmissionUiIntent.OnTextChange -> formState.update {
    it.copy(text = intent.text.take(MAX_TEXT_LENGTH), hasProfanity = false)
}

private fun checkPhotos(uris: List<String>) {
    val state = formState.value
    val newUris = uris.filterNot { it in state.photoUris }
        .take(SubmissionUiState.MAX_PHOTOS - state.photoUris.size - state.checkingPhotosCount)
    formState.update { it.copy(checkingPhotosCount = it.checkingPhotosCount + newUris.size, photoRejection = null) }
    newUris.forEach { uri ->
        viewModelScope.launch {
            val result = contentCheckRepository.checkPhoto(uri)
            formState.update { current ->
                current.copy(
                    checkingPhotosCount = current.checkingPhotosCount - 1,
                    photoUris = if (result == PhotoCheckResult.ALLOWED) {
                        (current.photoUris + uri).toPersistentList()
                    } else {
                        current.photoUris
                    },
                    photoRejection = result.takeIf { it != PhotoCheckResult.ALLOWED } ?: current.photoRejection
                )
            }
        }
    }
}

private fun send() {
    val state = formState.value
    if (!state.canSend) return
    if (contentCheckRepository.containsProfanity(state.text) || contentCheckRepository.containsProfanity(state.contact)) {
        formState.update { it.copy(hasProfanity = true) }
        return
    }
    ...
}
```

`SubmissionScreen` / `PhotoPickerRow`:
- пока идёт проверка, в ряду фото показываются `checkingPhotosCount` плиток-заглушек с `CircularProgressIndicator`;
- под рядом выводится `photoRejection`: `BLOCKED` → `photo_did_not_pass_check_msg`, `UNREADABLE` → `could_not_open_photo_msg`;
- у поля текста `isError = state.hasProfanity`, а `supportingText` показывает `remove_offensive_words_msg`.

---

## 5. Форма обратной связи

### Почему
Фото в обратной связи нет, поэтому достаточно проверить текст и контакт при отправке. Ошибка показывается так же, как в материалах.

`ui/feedback/models/FeedbackUiState.kt` получает `val hasProfanity: Boolean = false`. `FeedbackViewModel.send()` вызывает ту же проверку `containsProfanity(text) || containsProfanity(contact)` перед `feedbackRepository.send(...)`. `OnTextChange` и `OnContactChange` сбрасывают `hasProfanity`. В `FeedbackScreen` у поля сообщения `isError` и `supportingText`, как в разделе 4.

---

## 6. Строки (ru / be)

```xml
<string name="remove_offensive_words_msg">Уберите нецензурные выражения — такое сообщение не будет отправлено</string>
<string name="photo_did_not_pass_check_msg">Фото не прошло автоматическую проверку и не будет отправлено</string>
<string name="could_not_open_photo_msg">Не удалось открыть фото. Выберите другое</string>
<string name="checking_photo">Проверяем фото</string>
```
```xml
<string name="remove_offensive_words_msg">Прыбярыце непрыстойныя выразы — такое паведамленне не будзе адпраўлена</string>
<string name="photo_did_not_pass_check_msg">Фота не прайшло аўтаматычную праверку і не будзе адпраўлена</string>
<string name="could_not_open_photo_msg">Не атрымалася адкрыць фота. Выберыце іншае</string>
<string name="checking_photo">Правяраем фота</string>
```

## 7. Документация
- В `CLAUDE.md` в раздел про Firebase добавить абзац: обращения проходят через `ContentCheckRepository` (фильтр мата и TFLite-модель NSFW из `assets/`) до записи в базу; проверка работает на клиенте; модель собирается скриптом `tools/nsfw/convert_model.py`.
- В памятку редактора (раздел «Заявки и обращения») добавить строку: мат и непристойные фото отсекаются автоматически, но проверку можно обойти, поэтому модерация остаётся.
- В R8 ничего не нужно: LiteRT поставляет свои consumer-rules.

## Файлы
- Новые: `domain/models/PhotoCheckResult.kt`, `domain/repository/ContentCheckRepository.kt`, `data/contentcheck/{datasource/local/ProfanityDetector.kt, datasource/local/NsfwImageClassifier.kt, model/NsfwScores.kt, repository/ContentCheckRepositoryImpl.kt}`, `di/ContentCheckModule.kt`, `tools/nsfw/*`, `assets/nsfw_mobilenet_v2_224.tflite`.
- Изменяемые: `SubmissionViewModel`, `SubmissionUiState`, `SubmissionScreen`, `PhotoPickerRow`, `FeedbackViewModel`, `FeedbackUiState`, `FeedbackScreen`, `values*/strings.xml`, `libs.versions.toml`, `app/build.gradle.kts`, `CLAUDE.md`.
- Тесты не пишу: пользователь просил без тестов.

## Проверка

```bash
./gradlew detektAll
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew installDebug
```

Ручные сценарии:
1. **Мат в тексте материалов:** написать «ну ты и х у й», «пи3да», «XУЙ», «бляяядь». Отправка не происходит, поле подсвечено, в базе и в админке ничего нового.
2. **Ложные срабатывания:** «Хлебал солдатскую кашу», «застраховать», «хулиган», «мандарины на Новый год», «себе», «небо», «команда». Всё отправляется.
3. **Мат в контакте:** то же поведение, что в пункте 1.
4. **Обратная связь:** пункты 1–3 на экране «Написать нам».
5. **Фото:** выбрать обычное архивное фото и откровенное тестовое изображение. Во время проверки видна плитка со спиннером. Обычное фото добавляется, откровенное не появляется, под рядом сообщение «Фото не прошло проверку». В Storage `OurMemory/Submissions/…` лишних файлов нет.
6. **Битый файл:** выбрать повреждённое изображение. Появляется «Не удалось открыть фото».
7. **Производительность:** выбор 5 фото на среднем телефоне проверяется за пару секунд, интерфейс не зависает.
8. **Release-сборка:** проверки работают так же (модель читается из assets, R8 ничего не сломал).
9. **be:** все сообщения на белорусском.
