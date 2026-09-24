# Админ-флоу: продолжение (шаги 3–9)

## Контекст

Реализуем `claude/admin-flow-plan.ru.md` по шагам, после каждого шага — `assembleDebug detektAll testDebugUnitTest` и коммит в `master`.

Сделано:
- `fc2e31b` — фикс падения свечей (строка вместо числа в `Candles/{id}`).
- `8d4b2ed` — шаг 1: вход админа, `OurMemory/Admins/{uid}`, вкладка «Админ».
- `df9987a` — шаг 2: «Написать нам» / «Сообщить об ошибке», список обращений у админа.
- Шаг 3 (модерация) **написан, но не закоммичен**: сборка и detekt зелёные, тестов ещё нет.

Отличия от исходного плана, принятые по ходу:
- `FeedbackType`, `ModerationStatus` лежат в `domain/models` без `R`; подписи — расширения `labelRes` в `ui/*/models`.
- Подпись фото «Из семейного архива» приходит из UI (`stringResource`) в `SubmissionReviewUiIntent.OnApproveClick(photoCaption)`, чтобы в data не было захардкоженных строк.
- Общие помощники: `DatabaseReference.observeValue()`, `DataSnapshot.childrenAs<T>()` (пропускает мусор вроде текущего `Submissions = [null,""]`), `VeteranKeys.forId(id) = "veteran$id"`.
- Маршруты: `ui/navigation/ui/AdminGraph.kt`, `VeteranGraph.kt`, `TabNavigation.kt` (иначе detekt `LongMethod`).
- `VeteransRepository.invalidate()` (suspend, под `Mutex`) добавлен уже в шаге 3 — одобрение сразу сбрасывает кеш.

## Шаг 3. Модерация — довести и закоммитить

**Почему.** Код в рабочей копии, но без тестов; план требует покрыть `approvalUpdates`.

```kotlin
@Test
fun approvalAppendsTextAndSelectedPhotosAndMarksApproved() {
    val updates = approval(editedText = "  Письмо с фронта ", photos = listOf("https://a")).toUpdates(
        currentInfo = listOf("Абзац"),
        reviewer = "admin@memory.by",
        reviewedAt = REVIEWED_AT
    )
    assertEquals(listOf("Абзац", "Письмо с фронта", "https://a|Из семейного архива"), updates["Veterans/veteran10/veteransInfo"])
    assertEquals("approved", updates["Submissions/s1/status"])
}
```

Ещё тесты: пустой текст пропускается; `ModerationRepositoryImpl` сортирует `pending` первыми, отбрасывает записи без `id`, после `approve` вызывает `invalidate()`; `SubmissionReviewViewModel` — снятое фото не попадает в `approvedPhotoUrls`, отредактированный текст уходит в одобрение, ошибка → `hasFailed`, успех → `isFinished`. Фейки: `FakeModerationRepository`, `FakeModerationRemoteDataSource`. `AdminHomeViewModel` теперь берёт `ModerationRepository` — фейк нужен и там, если появится тест.

Коммит: `Let admins approve or reject relatives' submissions`.

## Шаг 4. Загрузка медиа

**Почему.** Портрет, фото и аудио из редакторов нужно класть в Storage и получать ссылку для базы; сжатие уже есть в `PhotoCompressor`.

```kotlin
class MediaUploader @Inject constructor(
    private val storage: FirebaseStorage,
    private val photoCompressor: PhotoCompressor,
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MediaRepository {
    override suspend fun uploadPhoto(uri: String, folder: String): String {
        val bytes = withContext(ioDispatcher) { photoCompressor.compress(uri.toUri()) }
        val reference = mediaReference(folder, "${UUID.randomUUID()}$JPEG_EXTENSION")
        reference.putBytes(bytes, storageMetadata { contentType = JPEG_TYPE }).await()
        return reference.downloadUrl.await().toString()
    }
}
```

Файлы: `data/media/datasource/remote/MediaUploader.kt`, `domain/repository/MediaRepository.kt`, `di/MediaModule.kt`. Путь — `OurMemory/Media/{folder}/{uuid}`.

## Шаг 5. Редактор ветеранов

**Почему.** Добавлять и править ветеранов без JSON в консоли. Запись целиком в `Veterans/veteran{id}` в текущем формате.

```kotlin
fun List<String>.toInfoBlocks(): List<InfoBlock> = map { entry ->
    if (entry.contains(LINK_MARKER)) {
        InfoBlock.Media(entry.substringBefore(DESCRIPTION_SEPARATOR), entry.substringAfter(DESCRIPTION_SEPARATOR, ""))
    } else {
        InfoBlock.Paragraph(entry)
    }
}
```

- `data/content/` — `ContentEditorRepositoryImpl` (`saveVeteran`, `deleteVeteran`, `nextVeteranId` = max числовой id + 1).
- `ui/admin/veterans/` — список с поиском и редактор: ФИО, годы, категория, тексты, награды со счётчиком (`"9,9,11"` ↔ `Map<Reward, Int>`), даты `yyyy-MM-dd` через `DatePickerDialog`, портрет, аудио (`OpenDocument("audio/*")`), место захоронения, блоки биографии (вверх/вниз/удалить).
- «Сохранить» неактивно без ФИО и при неверной дате.
- Тесты: обратимость `toInfoBlocks`/`toVeteransInfo`, награды ↔ счётчики, `nextVeteranId`, валидация в ViewModel.

## Шаг 6. Редактор мест захоронения

**Почему.** Экскурсии и метки строятся из мест; координаты удобно ставить у могилы.

```kotlin
val mapInputListener = remember {
    object : InputListener {
        override fun onMapTap(map: Map, point: Point) = onPointPicked(point.latitude, point.longitude)
        override fun onMapLongTap(map: Map, point: Point) = Unit
    }
}
```

`ui/admin/burials/` — тип, участок/ряд/место, описание, фото, мини-карта с меткой, «Моё местоположение» (переиспользовать логику `ui/map/ui/MyLocationAction.kt`), поля широты/долготы. Новый id — `push().key`, у старых (`b_001`) не меняется.

## Шаг 7. Редактор экскурсий

**Почему.** Экскурсии пока добавляются только импортом JSON.

`ui/admin/tours/` — название, описание, остановки (выбор места с поиском, текст, аудио), вверх/вниз/удалить; превью — существующий `ui/tours/ui/TourMap.kt`. Запись в `Tours/{id}`.

## Шаг 8. Свежие данные после правок

**Почему.** `BurialsRepositoryImpl` и `ToursRepositoryImpl` кешируют узел на весь процесс — после сохранения админ увидит старое.

```kotlin
override suspend fun invalidate() = mutex.withLock {
    cachedBurials = null
}
```

`ContentEditorRepositoryImpl` после успешной записи вызывает `invalidate()` нужного репозитория (у ветеранов метод уже есть).

## Шаг 9. Правила

**Почему.** Проверка роли в клиенте — удобство; защита — правила Firebase.

`firebase/database.rules.json`: `Admins/$uid` читает только сам uid; `Veterans`/`Burials`/`Tours` пишут админы; `Feedback`/`Submissions` — читают админы, создаёт любой вошедший, менять может только админ. `firebase/storage.rules`: `OurMemory/Media/**` читают все, пишут uid админов (список явно). Обновить `CLAUDE.md` (узлы `Media`, редакторы) и напомнить, что правила сливаются вручную в консоли.

## Проверка

1. После каждого шага: `./gradlew assembleDebug detektAll testDebugUnitTest`.
2. Вручную в Firebase: включить Email/Password, создать админа, записать `OurMemory/Admins/{uid}: true`, вставить uid в `storage.rules`, слить правила.
3. На устройстве:
   - посетитель не видит вкладку «Админ»; «Написать нам» и «Сообщить об ошибке» создают записи в `OurMemory/Feedback`;
   - неверный пароль → ошибка; аккаунт без `Admins` → «нет прав»; админ → вкладка со счётчиками;
   - заявка с 2 фото → снять одно, поправить текст, одобрить → в карточке новый абзац и одно фото «Из семейного архива», статус `approved`; отклонение → `rejected`, карточка не меняется;
   - редакторы ветерана/места/экскурсии: изменения видны без перезапуска;
   - выход — вкладка пропадает; запись в `Veterans` через REST без авторизации отклоняется правилами.
