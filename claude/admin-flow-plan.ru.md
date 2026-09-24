# Два флоу: посетитель и администратор

## Контекст

Сейчас приложение одно для всех. Всё, что не может сделать посетитель, делается вручную в Firebase Console:
- модерация материалов от родственников;
- добавление ветеранов, мест и экскурсий;
- загрузка аудио и фото.

Это неудобно и опасно: можно случайно стереть данные других приложений в общей базе `chatroom-85fb8`.

Нужно два флоу в одном приложении:
- **Посетитель** — всё, что есть сейчас, плюс форма «Написать нам» (обратная связь) и «Сообщить об ошибке» на карточке ветерана.
- **Администратор** — всё то же самое плюс вкладка «Админ»:
  - модерация материалов (правка и одобрение прямо в карточку ветерана, отклонение);
  - обратная связь (чтение, отметка «рассмотрено»);
  - редактор ветеранов: данные, награды, даты, портрет, аудио, медиа и абзацы биографии;
  - редактор мест захоронения: координаты по геолокации или тапом по карте (без мест экскурсии не собрать);
  - редактор экскурсий: остановки, текст, аудио.

Принятые решения:
- вход админа — e-mail и пароль (Firebase Auth), список админов — `OurMemory/Admins/{uid}`;
- медиа загружаются с телефона в Firebase Storage;
- одобренный материал сразу попадает в карточку ветерана;
- обратная связь — форма «Написать нам» и «Сообщить об ошибке» с привязкой к ветерану.

Пешеходный маршрут по дорожкам — позже, когда будут координаты дорожек.

Первое действие после одобрения — сохранить план в `claude/admin-flow-plan.ru.md`. Реализация идёт шагами, после каждого шага — `assembleDebug`, `detektAll`, `testDebugUnitTest` и коммит в `master`.

## Структура

```text
data/
├── auth/             AuthRepositoryImpl (сессия, вход, роль)
├── feedback/         FeedbackRepositoryImpl
├── moderation/       ModerationRepositoryImpl (заявки, одобрение, отклонение)
├── content/          ContentEditorRepositoryImpl (запись Veterans/Burials/Tours)
└── media/            MediaUploader (загрузка в Storage, сжатие фото — PhotoCompressor)
ui/
├── feedback/                       форма «Написать нам» (посетитель)
└── admin/
    ├── home/        вкладка «Админ»: счётчики, разделы, выход
    ├── login/       вход по e-mail
    ├── moderation/  список и разбор заявок
    ├── feedbacklist/
    ├── veterans/    список и редактор ветерана
    ├── burials/     список и редактор места
    └── tours/       список и редактор экскурсии
```

Каждая подпапка `ui/admin/*` — фича со своими `models/`, `ui/`, `viewmodels/`.

## Шаг 1. Роль и вход администратора

**Почему.** Все админские функции должны быть доступны только людям из списка `OurMemory/Admins`. Иначе любой, кто скачает приложение, сможет менять данные. Роль определяется по записи в базе, а не по флагу в приложении: правила базы проверяют ту же запись, поэтому обойти их из клиента нельзя.

```kotlin
data class AdminSession(
    val email: String = "",
    val isAdmin: Boolean = false
)
```

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @param:MemoryRoot private val root: DatabaseReference
) : AuthRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSession(): Flow<AdminSession> = observeUser().flatMapLatest { user ->
        if (user == null || user.isAnonymous) {
            flowOf(AdminSession())
        } else {
            observeExists(root.child(DatabaseNodes.ADMINS).child(user.uid))
                .map { isAdmin -> AdminSession(email = user.email.orEmpty(), isAdmin = isAdmin) }
        }
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override fun signOut() = auth.signOut()

    private fun observeUser() = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}
```

`observeExists` — общий `callbackFlow` поверх `ValueEventListener`, такой же, как в `CandlesRemoteDataSourceImpl`.

Навигация: `AppNavigation` получает `SessionViewModel` (`hiltViewModel()`) с `isAdmin: StateFlow<Boolean>`. В `TopLevelTab` добавляется `ADMIN`. `AppBottomBar` показывает эту вкладку, только когда `isAdmin == true`.

```kotlin
val tabs = TopLevelTab.entries.filter { it != TopLevelTab.ADMIN || isAdmin }
```

Вход: внизу вкладки «О кладбище» — строка «Вход для администратора» → `AdminLoginScreen` (e-mail, пароль, ошибка «Неверный e-mail или пароль» / «У этого аккаунта нет прав администратора»). После успешного входа открывается вкладка «Админ». Выход — кнопка на вкладке «Админ».

## Шаг 2. Обратная связь

**Почему.** Посетители замечают ошибки в датах, фамилиях, наградах, но сообщить о них некуда. Отдельный узел `Feedback` со статусом даёт админу очередь обращений. Привязка к ветерану позволяет сразу открыть нужную карточку.

```kotlin
enum class FeedbackType(@param:StringRes val labelRes: Int) {
    DATA_ERROR(R.string.data_error),
    SUGGESTION(R.string.suggestion),
    OTHER(R.string.other)
}
```

```kotlin
data class Feedback(
    val id: String = "",
    val type: String = FeedbackType.OTHER.name,
    val text: String = "",
    val contact: String = "",
    val veteranId: String = "",
    val status: String = STATUS_NEW,
    val createdAt: Long = 0L
)
```

Анонимный вход перед записью выносится из `SubmissionsRemoteDataSourceImpl` в общий `AnonymousSession.ensureSignedIn()` (если пользователь уже вошёл, например как админ, — ничего не делает).

Входы:
- **Посетитель:**
  - «Написать нам» на вкладке «О кладбище» → `feedback`;
  - «Сообщить об ошибке» на карточке ветерана рядом с «Дополнить историю» → `feedback?veteranId={id}`.

  Экран: чипы типа, текст, необязательный контакт, «Отправить» → «Спасибо».
- **Админ:** «Обратная связь» — список новых сверху, у обращения ссылка на ветерана и кнопка «Рассмотрено» (`status = "done"`).

## Шаг 3. Модерация

**Почему.** Материалы от родственников копятся в `Submissions` со статусом `pending`, и сейчас их никто не видит. Админ правит текст, снимает лишние фото и одобряет. Одобрение одной атомарной записью (`updateChildren`) дописывает материал в карточку и меняет статус заявки. Так не бывает ситуации, когда материал добавлен, а заявка всё ещё `pending`, или наоборот.

Экран разбора: ФИО ветерана, редактируемый текст, контакт, фото с переключателями «добавить в карточку», кнопки «Одобрить» и «Отклонить».

Одобрение — чистая функция, её легко покрыть тестом:

```kotlin
fun approvalUpdates(
    veteranKey: String,
    currentInfo: List<String>,
    submissionId: String,
    editedText: String,
    approvedPhotoUrls: List<String>,
    reviewer: String
): Map<String, Any> {
    val newInfo = currentInfo +
        listOfNotNull(editedText.trim().takeIf { it.isNotEmpty() }) +
        approvedPhotoUrls.map { "$it$DESCRIPTION_SEPARATOR$FAMILY_ARCHIVE_CAPTION" }
    return mapOf(
        "${DatabaseNodes.VETERANS}/$veteranKey/veteransInfo" to newInfo,
        "${DatabaseNodes.SUBMISSIONS}/$submissionId/status" to STATUS_APPROVED,
        "${DatabaseNodes.SUBMISSIONS}/$submissionId/reviewedBy" to reviewer,
        "${DatabaseNodes.SUBMISSIONS}/$submissionId/reviewedAt" to ServerValue.TIMESTAMP
    )
}
```

```kotlin
root.updateChildren(approvalUpdates(...)).await()
```

Фото заявки уже лежат в Storage. Для карточки берётся их `downloadUrl` (со встроенным токеном), поэтому посетители видят фото, хотя чтение `Submissions` в Storage закрыто. Подпись — строка «Из семейного архива». Отклонение пишет `status = "rejected"`, заявка остаётся в архиве.

## Шаг 4. Загрузка медиа

**Почему.** Портрет, фото в галерею и аудио админ выбирает на телефоне. Нужна одна точка загрузки: она сжимает фото уже готовым `PhotoCompressor`, кладёт файл в `OurMemory/Media/...` и возвращает ссылку для записи в базу.

```kotlin
class MediaUploader @Inject constructor(
    private val storage: FirebaseStorage,
    private val photoCompressor: PhotoCompressor,
    @ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun uploadPhoto(uri: Uri, folder: String): String {
        val bytes = withContext(ioDispatcher) { photoCompressor.compress(uri) }
        val reference = storage.getReference("${DatabaseNodes.ROOT}/Media/$folder/${UUID.randomUUID()}.jpg")
        reference.putBytes(bytes, storageMetadata { contentType = JPEG_TYPE }).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun uploadAudio(uri: Uri, folder: String): String {
        val type = context.contentResolver.getType(uri) ?: DEFAULT_AUDIO_TYPE
        val reference = storage.getReference("${DatabaseNodes.ROOT}/Media/$folder/${UUID.randomUUID()}")
        reference.putFile(uri, storageMetadata { contentType = type }).await()
        return reference.downloadUrl.await().toString()
    }
}
```

## Шаг 5. Редактор ветеранов

**Почему.** Добавлять и исправлять ветеранов нужно без JSON в консоли. Редактор пишет ветерана целиком в `Veterans/veteran{id}` — в том же формате, что уже в базе (ключ `veteran{id}`, `veteransInfo` — список абзацев и ссылок `url|описание`). Поэтому старые записи и новые читаются одинаково.

Экран «Ветераны» (админ) — поиск и список, «+» создаёт нового. Новый id — максимальный числовой id + 1.

Поля редактора:
- ФИО, годы жизни, категория (чипы «Герои СССР» / «Деятели искусств»);
- краткая информация, основной текст;
- награды — сетка иконок `Reward` со счётчиком (повторное нажатие даёт ×2), сохраняется в прежнем формате `"9,9,11"`;
- даты рождения и смерти — `DatePickerDialog`, пишутся как `yyyy-MM-dd`;
- портрет — выбор фото → `MediaUploader.uploadPhoto(uri, "veterans/$id")`;
- аудио — выбор файла (`OpenDocument("audio/*")`) → `uploadAudio`, прослушивание через существующий `AudioRepository`, «Удалить аудио»;
- место захоронения — выбор из списка мест (`BurialsRepository`);
- биография и медиа — список блоков «абзац» или «фото с подписью»: добавить, изменить, переместить вверх или вниз, удалить.

Сохранение, удаление с подтверждением.

```kotlin
sealed interface InfoBlock {
    data class Paragraph(val text: String) : InfoBlock
    data class Media(val url: String, val caption: String) : InfoBlock
}
```

```kotlin
fun List<String>.toInfoBlocks(): List<InfoBlock> = map { entry ->
    if (entry.contains(LINK_MARKER)) {
        InfoBlock.Media(entry.substringBefore(DESCRIPTION_SEPARATOR), entry.substringAfter(DESCRIPTION_SEPARATOR, ""))
    } else {
        InfoBlock.Paragraph(entry)
    }
}

fun List<InfoBlock>.toVeteransInfo(): List<String> = map { block ->
    when (block) {
        is InfoBlock.Paragraph -> block.text
        is InfoBlock.Media -> if (block.caption.isBlank()) block.url else "${block.url}$DESCRIPTION_SEPARATOR${block.caption}"
    }
}
```

Проверка перед сохранением: ФИО не пустое, даты в формате `yyyy-MM-dd` или пустые. Кнопка «Сохранить» неактивна, пока есть ошибки.

## Шаг 6. Редактор мест захоронения

**Почему.** Экскурсии и метки на карте строятся из мест. Координаты удобнее всего ставить прямо у могилы: кнопка «Моё местоположение» берёт текущую точку, а тап по карте позволяет поправить метку. Номер участка, ряда и места вводится текстом — он точнее GPS.

Экран списка мест и редактор:
- тип (могила / братская могила / памятник);
- участок, ряд, место;
- описание, фото (`uploadPhoto`);
- координаты: мини-карта с перетаскиваемой меткой, кнопка «Моё местоположение» (использует `LocationManager.requestSingleUpdate`, как `rememberMyLocationAction`), поля широты и долготы.

```kotlin
val mapInputListener = remember {
    object : InputListener {
        override fun onMapTap(map: Map, point: Point) = onPointPicked(point.latitude, point.longitude)
        override fun onMapLongTap(map: Map, point: Point) = Unit
    }
}
```

Слушатель — в `remember`, потому что MapKit держит его по слабой ссылке. Место пишется в `Burials/{id}`. Для новых мест id — `push().key`, у существующих (`b_001`…) id не меняется.

## Шаг 7. Редактор экскурсий

**Почему.** Экскурсии пока можно добавить только JSON-импортом. В редакторе админ собирает маршрут из существующих мест, пишет текст к каждой остановке и прикрепляет аудиогид. Порядок остановок определяет нумерацию на карте.

Поля:
- название, описание;
- остановки: «Добавить остановку» → выбор места из списка (поиск по ФИО похороненных, номеру участка или типу), текст, аудио (`uploadAudio`);
- кнопки вверх/вниз, удалить.

Превью — существующий `TourMap` с линией маршрута. Сохранение в `Tours/{id}`.

## Шаг 8. Свежие данные после правок

**Почему.** Репозитории загружают `Veterans`, `Burials`, `Tours` один раз за запуск. Без сброса кеша админ после сохранения увидит старые данные до перезапуска. Запись идёт через `ContentEditorRepository`, он же после успешного сохранения сбрасывает кеш нужного репозитория.

```kotlin
interface VeteransRepository {
    suspend fun getAllVeterans(): List<Veteran>
    suspend fun getHrefFromLink(publicKey: String): YandexImage
    suspend fun resolveDirectUrl(url: String): String
    fun invalidate()
}
```

```kotlin
override fun invalidate() {
    cachedVeterans = null
}
```

Так же — `BurialsRepository` и `ToursRepository`.

## Шаг 9. Правила

**Почему.** Проверка роли в приложении — только удобство. Реальная защита — правила Firebase. Писать `Veterans`, `Burials`, `Tours`, читать заявки и обратную связь могут только uid из `Admins`. Посетители могут только создавать новые заявки и обращения.

`firebase/database.rules.json` (внутри `OurMemory`):

```json
"Admins": {
  "$uid": { ".read": "auth != null && auth.uid === $uid", ".write": false }
},
"Veterans": {
  ".read": true,
  ".write": "auth != null && root.child('OurMemory/Admins/' + auth.uid).exists()"
},
"Feedback": {
  ".read": "auth != null && root.child('OurMemory/Admins/' + auth.uid).exists()",
  "$id": {
    ".write": "auth != null && (!data.exists() || root.child('OurMemory/Admins/' + auth.uid).exists())"
  }
}
```

`Burials` и `Tours` — как `Veterans`. `Submissions` — как `Feedback`.

Storage-правила не умеют читать Realtime Database, поэтому uid админов в `firebase/storage.rules` перечисляются явно:

```
function isAdmin() {
  return request.auth != null && request.auth.uid in ['<uid админа>'];
}
match /OurMemory/Media/{allPaths=**} {
  allow read: if true;
  allow write: if isAdmin();
}
match /OurMemory/Submissions/{submissionId}/{file} {
  allow read: if isAdmin();
  allow create: if request.auth != null
    && request.resource.size < 10 * 1024 * 1024
    && request.resource.contentType.matches('image/.*');
}
```

## Строки

Все новые надписи — в `values/` и `values-be/`, ключи по содержимому. Например: `admin`, `sign_in_as_admin`, `email`, `password`, `sign_in`, `sign_out`, `wrong_email_or_password_msg`, `no_admin_rights_msg`, `moderation`, `approve`, `reject`, `feedback`, `write_to_us`, `report_an_error`, `data_error`, `suggestion`, `other`, `reviewed`, `veterans`, `new_veteran`, `save`, `delete`, `delete_veteran_msg`, `add_paragraph`, `add_photo`, `add_audio`, `my_location`, `burial_places`, `new_burial_place`, `add_stop`, `from_family_archive`.

## Критичные файлы

- новые: `data/{auth,feedback,moderation,content,media}/*`, `domain/models/{AdminSession,Feedback,FeedbackType}.kt`, `domain/repository/{AuthRepository,FeedbackRepository,ModerationRepository,ContentEditorRepository}.kt`, `ui/feedback/*`, `ui/admin/**`
- `data/firebase/DatabaseNodes.kt` — `ADMINS`, `FEEDBACK`
- `data/submissions/datasource/remote/SubmissionsRemoteDataSourceImpl.kt` — вход вынести в `AnonymousSession`
- `data/repository/VeteransRepositoryImpl.kt`, `data/burials/…`, `data/tours/…` — `invalidate()`
- `ui/navigation/models/TopLevelTab.kt`, `ui/navigation/ui/{AppNavigation,AppBottomBar}.kt` — вкладка «Админ» и новые маршруты
- `ui/info/ui/InfoScreen.kt` — «Написать нам», «Вход для администратора»
- `ui/details/ui/DetailsScreen.kt` — «Сообщить об ошибке»
- `firebase/database.rules.json`, `firebase/storage.rules`, `CLAUDE.md`, `res/values*/strings.xml`

## Проверка

1. После каждого шага: `./gradlew assembleDebug detektAll testDebugUnitTest`.
2. Unit-тесты:
   - `AuthRepository`: анонимный пользователь → не админ; uid есть в `Admins` → админ;
   - `approvalUpdates`: текст и выбранные фото добавляются в конец `veteransInfo`, пустой текст пропускается, статус `approved`;
   - `toInfoBlocks` / `toVeteransInfo` — обратимы на реальном формате (`"url|подпись"`, ссылка без подписи, абзац);
   - формат наград `"9,9,11"` ↔ счётчики редактора;
   - следующий id ветерана = максимальный + 1, нечисловые id игнорируются;
   - ViewModel редактора: «Сохранить» неактивно без ФИО и при неверной дате;
   - форма обратной связи: отправка требует текст, тип и ветеран попадают в запись.
3. Настройка Firebase (вручную):
   - Authentication → Email/Password → Enable;
   - создать пользователя-админа и записать его uid в `OurMemory/Admins/{uid}: true`;
   - вставить uid в `firebase/storage.rules`;
   - слить правила в консоли.
4. Сценарии на устройстве:
   - Посетитель: вкладки «Ветераны / Карта / О кладбище», вкладки «Админ» нет. «Написать нам» и «Сообщить об ошибке» создают записи в `OurMemory/Feedback`.
   - Вход с неверным паролем → ошибка. Вход аккаунтом без записи в `Admins` → «нет прав», вкладки нет. Вход админом → появляется вкладка «Админ» со счётчиками.
   - Модерация: отправить материал с 2 фото как посетитель → в админке заявка. Поправить текст, снять одно фото, одобрить → в карточке ветерана новый абзац и одно фото «Из семейного архива», заявка в статусе `approved`. Отклонить другую → `rejected`, карточка не меняется.
   - Редактор: создать ветерана с портретом, датами, наградами ×2, аудио и фото → он появляется в списке «Ветераны» без перезапуска, аудио играет, «В этот день» срабатывает на сегодняшнюю дату.
   - Место: у могилы «Моё местоположение» → точка на карте. Сохранить → метка на карте «Карта».
   - Экскурсия: собрать из 3 мест с аудио → появляется кнопка «Экскурсии», экран экскурсии показывает линию и аудио.
   - Выход: вкладка «Админ» пропадает. Запись в `Veterans` через REST без авторизации отклоняется правилами.
