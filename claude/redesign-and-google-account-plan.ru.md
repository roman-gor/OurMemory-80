# Освежить навигацию, «Ещё» и админку, добавить вход через Google и памятку редактора

## Контекст

Замечания пользователя:
- **Нижняя панель.** Подпись «О кладбище» едва влезает. Когда появляется пятая вкладка «Админ», подписи сжимаются и обрезаются.
- **«Ещё».** Все элементы свалены в одну колонку. Тема, размер текста и язык выбираются сегментными переключателями, где подписи обрезаются. Напоминания подписаны непонятно: «9 Мая — День Победы» и «Памятные дни избранных» ничего не объясняют.
- **Админка.** Главный экран — просто список одинаковых блоков. Памятка редактора есть только в файле `claude/editor-guide.html`, в приложении её нет.
- **Сохранность данных.** Избранное лежит в DataStore на телефоне. Обращения привязаны к анонимному uid. После переустановки теряется и то и другое. Решение пользователя: вход через Google.

Что пользователь выбрал:
1. В нижней панели подпись видна только у активной вкладки.
2. Оба напоминания остаются, но с понятными названиями и пояснениями.
3. Вход через Google, чтобы избранное и обращения сохранялись после переустановки.

После одобрения я первым делом скопирую этот план в `claude/redesign-and-google-account-plan.ru.md`, как требует CLAUDE.md.

---

## 1. Нижняя панель: подпись только у активной вкладки

### Почему
Сейчас в `AppBottomBar` каждая вкладка получает `weight(1f)` и выводит подпись `labelSmall`. На 5 вкладок у подписи остаётся примерно 60 dp, поэтому «О кладбище» и «Ветераны» обрезаются. Если подпись показывать только у активной вкладки, а у остальных оставить иконку, панель вмещает 5 вкладок с запасом.

### Код
`ui/navigation/ui/AppBottomBar.kt`: у пунктов убираем `weight`, выравниваем их через `SpaceAround`, а активный пункт раскрывается анимацией.

```kotlin
@Composable
private fun FloatingTabItem(tab: TopLevelTab, isSelected: Boolean, onClick: () -> Unit) {
    val label = stringResource(tab.labelRes)
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(containerColor)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = ITEM_HORIZONTAL_PADDING, vertical = ITEM_VERTICAL_PADDING)
            .animateContentSize()
    ) {
        Icon(painter = painterResource(tab.iconRes), contentDescription = null, tint = contentColor)
        AnimatedVisibility(visible = isSelected, enter = expandHorizontally() + fadeIn(), exit = shrinkHorizontally() + fadeOut()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
                modifier = Modifier.padding(start = LABEL_START_PADDING)
            )
        }
    }
}
```

Внешний `Row` получает `horizontalArrangement = Arrangement.SpaceAround`. Константы `ITEM_*` и `LABEL_START_PADDING` выносятся в top-level `private val`, как уже сделано с `BAR_HEIGHT`.

---

## 2. Общие компоненты для групп настроек

### Почему
И «Ещё», и главный экран админки сейчас собраны из одинаковых `LinkRow`-карточек. Смысловых групп и иконок нет, поэтому экраны читаются как свалка. Нужны два общих компонента, чтобы оба экрана выглядели единообразно: группа со скруглённым фоном и строка с цветной иконкой, заголовком, подзаголовком и слотом справа.

### Код
`ui/common/ui/SettingsGroup.kt`

```kotlin
@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GROUP_TITLE_SPACING)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = GROUP_TITLE_START_PADDING)
        )
        Surface(
            shape = RoundedCornerShape(GROUP_CORNER_RADIUS),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}
```

`ui/common/ui/SettingRow.kt`: вся строка кликабельна, иконка стоит в тонированном круге, справа слот `trailing`.

```kotlin
@Composable
fun SettingRow(
    @DrawableRes iconRes: Int,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = { ChevronIcon() }
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ROW_SPACING),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(ICON_CONTAINER_SIZE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing()
    }
}
```

Между строками внутри группы ставится `HorizontalDivider` с отступом слева на ширину иконки. Это делает private-функция `GroupDivider()` в `SettingsGroup.kt`.

---

## 3. Экран «Ещё»

### Почему
- Сегментные кнопки с тремя вариантами обрезают «Как в системе» и «Очень крупный». Вместо них будет строка «Тема · Как в системе», по нажатию раскрывается список вариантов с галочкой.
- Названия напоминаний не объясняют, что они делают. Добавляю подзаголовки и иконки.
- У экрана нет ни одного тёплого акцента. Сверху добавляю баннер с вечным огнём и строкой «Никто не забыт, ничто не забыто», а под ним карточку аккаунта (шаг 4).

### Макет
```
┌──────────────────────────────────┐
│ 🔥  Никто не забыт,              │  ← MemoryBanner, градиент primary → MemoryRedBright
│     ничто не забыто              │
└──────────────────────────────────┘
┌──────────────────────────────────┐
│ (G) Сохраните избранное          │  ← AccountCard
│     и обращения · [Войти]        │
└──────────────────────────────────┘
МОЁ
  ✉ Мои обращения            (2) ›
  ♥ Избранное                    ›
  ⌗ Сканировать QR-код            ›
  ✎ Написать нам                  ›
ОФОРМЛЕНИЕ
  ◐ Тема          Как в системе ⌄
  Aa Размер текста      Обычный ⌄
  🌐 Язык               Русский ⌄
НАПОМИНАНИЯ
  ★ День Победы                 [●]
    Утром 9 мая напомним почтить память
  🔔 Памятные даты избранных     [●]
    В дни рождения и памяти ветеранов из избранного
          Вход для администратора
```

### Код
`ui/more/ui/ChoiceSettingRow.kt` заменяет `ChoiceSetting`. Вся строка открывает `DropdownMenu` со скруглённой формой и галочкой у выбранного варианта.

```kotlin
@Composable
fun <T> ChoiceSettingRow(
    @DrawableRes iconRes: Int,
    title: String,
    options: ImmutableList<T>,
    selected: T,
    labelRes: (T) -> Int,
    onSelect: (T) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Box {
        SettingRow(
            iconRes = iconRes,
            title = title,
            onClick = { isExpanded = true },
            trailing = { SelectedValue(text = stringResource(labelRes(selected)), isExpanded = isExpanded) }
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = RoundedCornerShape(MENU_CORNER_RADIUS),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            offset = DpOffset(x = MENU_OFFSET_X, y = 0.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(labelRes(option))) },
                    trailingIcon = {
                        if (option == selected) {
                            Icon(painter = painterResource(R.drawable.check), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = {
                        isExpanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}
```

`SelectedValue` — это private-функция в том же файле. Она выводит значение цветом `primary` и стрелку `keyboard_arrow_down`, которая поворачивается через `animateFloatAsState` на `ARROW_EXPANDED_ROTATION`.

`ui/more/ui/SwitchSettingRow.kt`: `SettingRow`, у которого в `trailing` стоит `Switch`, а `onClick` переключает значение.

```kotlin
@Composable
fun SwitchSettingRow(@DrawableRes iconRes: Int, title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingRow(
        iconRes = iconRes,
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!isChecked) },
        trailing = { Switch(checked = isChecked, onCheckedChange = onCheckedChange) }
    )
}
```

`ui/more/ui/MemoryBanner.kt`: `Box` с `Brush.linearGradient(MemoryRed, MemoryRedBright)`, иконкой `flame` с лёгкой пульсацией через `rememberInfiniteTransition` (alpha от `FLAME_MIN_ALPHA` до 1) и текстом `never_forgotten_msg`.

`MoreScreen.kt` собирается из `MemoryBanner`, `AccountCard` и трёх `SettingsGroup`. Старый `SettingControls.kt` удаляется.

Строки ru/be:
```xml
<string name="never_forgotten_msg">Никто не забыт, ничто не забыто</string>
<string name="mine">Моё</string>
<string name="victory_day">День Победы</string>
<string name="in_the_morning_of_may_9_msg">Утром 9 мая напомним почтить память</string>
<string name="memorable_dates_of_favorites">Памятные даты избранных</string>
<string name="on_birthdays_and_memorial_days_msg">В дни рождения и памяти ветеранов из избранного</string>
```

---

## 4. Вход через Google: избранное и обращения не теряются

### Почему
- **Обращения** ищутся запросом `orderByChild("authorUid").equalTo(uid)`. После переустановки Firebase выдаёт новый анонимный uid, поэтому старые обращения пропадают из «Мои обращения».
- **Избранное** хранится только в DataStore.

Если **привязать** Google-аккаунт к текущему анонимному пользователю (`linkWithCredential`), uid останется прежним. Все уже отправленные обращения сохранятся, а после переустановки вход через тот же Google вернёт тот же uid. Избранное переносим в Firebase: `OurMemory/Users/{uid}/favorites/{veteranId}: true`.

### Что нужно сделать в консоли (без этого вход не заработает)
В `app/google-services.json` сейчас пустой `oauth_client`, то есть Google-провайдер ещё не настроен. Нужно:
1. В Firebase открыть Authentication → Sign-in method и включить Google.
2. В Project settings добавить SHA-1 для debug-ключа (`./gradlew signingReport`) и для release-ключа.
3. Скачать новый `google-services.json`. После этого плагин сгенерирует `R.string.default_web_client_id`.

### 4.1 Зависимости
`gradle/libs.versions.toml`
```toml
[versions]
credentials = "1.5.0"
googleid = "1.1.1"

[libraries]
androidx-credentials = { group = "androidx.credentials", name = "credentials", version.ref = "credentials" }
androidx-credentials-play-services = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentials" }
googleid = { group = "com.google.android.libraries.identity.googleid", name = "googleid", version.ref = "googleid" }
```
Точные версии сверю с актуальными при реализации.

### 4.2 Домен
`domain/models/VisitorAccount.kt`
```kotlin
data class VisitorAccount(
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
)
```
`domain/models/GoogleSignInResult.kt`
```kotlin
enum class GoogleSignInResult { LINKED, SIGNED_IN, FAILED }
```
`domain/repository/VisitorAccountRepository.kt`
```kotlin
interface VisitorAccountRepository {
    fun observeAccount(): Flow<VisitorAccount?>
    suspend fun signInWithGoogle(idToken: String): GoogleSignInResult
    fun signOut()
}
```

### 4.3 Данные: привязка или вход
`data/account/datasource/remote/GoogleAccountRemoteDataSourceImpl.kt`
```kotlin
override suspend fun signInWithGoogle(idToken: String): GoogleSignInResult {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    val current = auth.currentUser
    return try {
        if (current != null && current.isAnonymous) {
            current.linkWithCredential(credential).await()
            GoogleSignInResult.LINKED
        } else {
            auth.signInWithCredential(credential).await()
            GoogleSignInResult.SIGNED_IN
        }
    } catch (collision: FirebaseAuthUserCollisionException) {
        auth.signInWithCredential(collision.updatedCredential ?: credential).await()
        GoogleSignInResult.SIGNED_IN
    } catch (_: FirebaseException) {
        GoogleSignInResult.FAILED
    }
}
```
`FirebaseAuthUserCollisionException` означает, что этот Google-аккаунт уже был привязан раньше, например до переустановки. В этом случае входим в него, и возвращается старый uid вместе со старыми обращениями.

В `AuthUser` добавляются `displayName` и `photoUrl`, в маппере `toAuthUser()` — соответствующие поля. `observeAccount()` возвращает `VisitorAccount`, только если пользователь не анонимный и среди провайдеров есть `GoogleAuthProvider.PROVIDER_ID`. Так администратор с e-mail не показывается как посетитель.

### 4.4 Избранное с синхронизацией
`data/favorites/datasource/remote/FavoritesRemoteDataSourceImpl.kt`
```kotlin
class FavoritesRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : FavoritesRemoteDataSource {

    override fun observe(uid: String) = favorites(uid).observeValue().map { snapshot ->
        snapshot.children.mapNotNull { it.key }.toSet()
    }

    override suspend fun set(uid: String, veteranId: String, isFavorite: Boolean) {
        favorites(uid).child(veteranId).setValue(if (isFavorite) true else null).await()
    }

    override suspend fun addAll(uid: String, veteranIds: Set<String>) {
        favorites(uid).updateChildren(veteranIds.associateWith { true }).await()
    }

    private fun favorites(uid: String) = root.child(DatabaseNodes.USERS).child(uid).child(DatabaseNodes.FAVORITES)
}
```
Локальная часть переезжает из `FavoritesRepositoryImpl` в `data/favorites/datasource/local/FavoritesLocalDataSourceImpl.kt`. Остальные слои выглядят так:

`FavoritesRepositoryImpl`
```kotlin
override fun observeFavorites() = accountSource.observeGoogleUid().flatMapLatest { uid ->
    if (uid == null) localDataSource.observe() else remoteDataSource.observe(uid)
}

override suspend fun toggle(veteranId: String) {
    val isFavorite = veteranId !in observeFavorites().first()
    localDataSource.set(veteranId, isFavorite)
    accountSource.observeGoogleUid().first()?.let { remoteDataSource.set(it, veteranId, isFavorite) }
}

override suspend fun mergeLocalIntoAccount(uid: String) {
    remoteDataSource.addAll(uid, localDataSource.observe().first())
}
```
`VisitorAccountRepositoryImpl.signInWithGoogle` после успешного входа вызывает `mergeLocalIntoAccount(uid)`: всё, что посетитель отметил до входа, попадает в аккаунт. Воркер годовщин (`FavoriteAnniversaryWorker`) продолжает читать `observeFavorites().first()`, а сессия Firebase сохраняется между запусками. Offline persistence уже включена, так что без сети отметки тоже работают.

`DatabaseNodes`: добавляются `USERS = "Users"` и `FAVORITES = "favorites"`.

### 4.5 Правила базы
`firebase/database.rules.json` (переносится в консоль вручную, внутрь `OurMemory`):
```json
"Users": {
  "$uid": {
    ".read": "auth != null && auth.uid === $uid",
    ".write": "auth != null && auth.uid === $uid",
    "favorites": {
      "$veteranId": { ".validate": "newData.val() === true" }
    }
  }
}
```

### 4.6 UI: получение idToken
Credential Manager нужен `Activity`-контекст, поэтому запрос живёт в Compose, а ViewModel получает только токен.
`ui/more/ui/GoogleSignInAction.kt`
```kotlin
@Composable
fun rememberGoogleSignInAction(onIdToken: (String) -> Unit, onFailure: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clientId = stringResource(R.string.default_web_client_id)
    val credentialManager = remember(context) { CredentialManager.create(context) }
    return remember(clientId) {
        {
            scope.launch {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
                    .build()
                try {
                    val credential = credentialManager.getCredential(context, request).credential
                    onIdToken(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                } catch (_: GetCredentialException) {
                    onFailure()
                }
            }
        }
    }
}
```
В `MoreUiState` появляется `account: VisitorAccount?`. В `MoreUiIntent` — `OnGoogleIdToken(idToken)`, `OnGoogleSignInFailed` и `OnSignOutClick`. Ошибку входа показываем через `SnackbarHostState` строкой `could_not_sign_in_msg`.

`ui/more/ui/AccountCard.kt` показывает одно из двух состояний:
- **Не вошёл:** иконка `cloud_sync` и текст `keep_favorites_and_requests_msg` («Войдите через Google, и избранное с обращениями сохранятся даже после переустановки»), под ним кнопка `sign_in_with_google`.
- **Вошёл:** аватар через Coil `AsyncImage` с `CircleShape`, имя, e-mail и `TextButton` «Выйти».

### Компромисс, о котором стоит знать
`AuthRepository.signIn` для администратора заменяет текущего пользователя. Если на одном телефоне войти как администратор, Google-вход посетителя будет сброшен. У редакторов обычно свои телефоны, так что это допустимо, но при реализации я добавлю об этом строку в памятку.

---

## 5. Главный экран админки

### Почему
Сейчас это заголовок, e-mail и пять одинаковых `LinkRow`: нельзя быстро понять, что требует внимания, а что относится к контенту. Разделяю экран на три части: шапка с аккаунтом, блок «Требует внимания» со счётчиками и сетка разделов контента с количеством записей.

### Макет
```
╭──────────────────────────────────╮
│ 🛡 Администрирование        ⎋    │  ← градиентная шапка, кнопка выхода
│ (Р) roman@…                      │
╰──────────────────────────────────╯
ТРЕБУЕТ ВНИМАНИЯ
┌──────────────┐ ┌──────────────┐
│  3           │ │  0           │   ← StatTile: primaryContainer, если > 0
│ Модерация    │ │ Обращения    │
└──────────────┘ └──────────────┘
КОНТЕНТ
┌──────────────┐ ┌──────────────┐
│ 👤 Ветераны   │ │ 🏛 Места      │   ← ContentTile в 2 колонки
│ 42 карточки  │ │ 58 мест      │
└──────────────┘ └──────────────┘
┌──────────────┐ ┌──────────────┐
│ 🚶 Экскурсии  │ │ 📖 Памятка    │
│ 3 маршрута   │ │ Как добавлять│
└──────────────┘ └──────────────┘
```

### Код
`AdminHomeUiState` получает счётчики контента:
```kotlin
data class AdminHomeUiState(
    val email: String = "",
    val pendingSubmissionsCount: Int = 0,
    val newFeedbackCount: Int = 0,
    val veteransCount: Int = 0,
    val burialsCount: Int = 0,
    val toursCount: Int = 0
)
```
`AdminHomeViewModel` добавляет в `combine` разовые загрузки из закэшированных репозиториев:
```kotlin
private fun observeContentCounts() = flow {
    emit(ContentCounts(
        veterans = veteransRepository.getAllVeterans().size,
        burials = burialsRepository.getAllBurials().size,
        tours = toursRepository.getAllTours().size
    ))
}.onStart { emit(ContentCounts()) }.catch { emit(ContentCounts()) }.flowOn(ioDispatcher)
```
`ContentCounts` — отдельный файл в `ui/admin/home/models/`.

Новые файлы в `ui/admin/home/ui/`: `AdminHeader.kt` (градиент, иконка `shield`, аватар с первой буквой e-mail, `IconButton` с `logout`), `StatTile.kt`, `ContentTile.kt`. Сетка собирается из `Row` с двумя `weight(1f)`-плитками внутри `LazyColumn`, потому что плиток всего четыре.

```kotlin
@Composable
fun StatTile(count: Int, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hasNew = count > 0
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(TILE_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = if (hasNew) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(TILE_PADDING)) {
            Text(text = count.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
```
Для подписей количества используются `plurals`: `veteran_cards_count`, `burial_places_count` и `routes_count`, как уже устроены `stops_count` и `candles_count`.

---

## 6. Списки админки и экран входа

### Почему
Во всех трёх списках (ветераны, места, экскурсии) стоит голый `OutlinedTextField` и безымянный круглый FAB «+». Новый редактор не понимает, с чего начинать. Сделаю единый стиль и подсказку прямо в списке.

### Код
- `ui/admin/common/ui/AdminSearchField.kt`: `TextField` в форме капсулы, с иконкой `search_icon`, цветом `surfaceContainerHigh` и без подчёркивания. Заменяет `OutlinedTextField` во всех трёх списках.
- `ExtendedFloatingActionButton` с текстом «Добавить ветерана» / «Добавить место» / «Добавить экскурсию» вместо безымянного `+`.
- `ui/admin/common/ui/GuideHintCard.kt`: тонкая карточка «Как добавить ветерана? Откройте памятку →» в начале списка. По нажатию открывается памятка сразу на нужном разделе.
```kotlin
@Composable
fun GuideHintCard(title: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(HINT_CORNER_RADIUS), color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(HINT_PADDING)) {
            Icon(painter = painterResource(R.drawable.menu_book), contentDescription = null)
            Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(horizontal = HINT_TEXT_PADDING))
            Icon(painter = painterResource(R.drawable.keyboard_arrow_right), contentDescription = null)
        }
    }
}
```
- `AdminLoginScreen`: та же градиентная шапка с иконкой `shield`, поля в карточке `surfaceContainerLow` и кнопка на всю ширину.

Редакторы (`VeteranEditorScreen`, `BurialEditorScreen`, `TourEditorScreen`) остаются как есть, потому что это отдельный большой объём. Если нужно, освежу их следующей итерацией.

---

## 7. Памятка редактора внутри приложения

### Почему
`claude/editor-guide.html` — это файл в репозитории. Редактор в приложении его не видит. Переношу основное содержание в экран админки и оставляю только то, что нужно редактору. Настройку Firebase и сборку QR-листа делает разработчик, они остаются в HTML.

### Код
`ui/admin/guide/models/GuideSection.kt`
```kotlin
enum class GuideSection(
    @param:StringRes val titleRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:ArrayRes val stepsRes: Int,
    @param:StringRes val tipRes: Int
) {
    BURIAL(R.string.burial_place, R.drawable.monument, R.array.burial_place_steps, R.string.check_new_marker_msg),
    VETERAN(R.string.veteran, R.drawable.person, R.array.veteran_steps, R.string.save_button_disabled_msg),
    TOUR(R.string.tour, R.drawable.directions_walk, R.array.tour_steps, R.string.stop_order_sets_route_msg),
    VOICE_OVER(R.string.voice_over, R.drawable.mic, R.array.voice_over_steps, R.string.check_stresses_before_msg),
    REQUESTS(R.string.requests_and_messages, R.drawable.mail, R.array.requests_steps, R.string.reply_shown_to_author_msg),
    TROUBLESHOOTING(R.string.if_something_is_wrong, R.drawable.help, R.array.troubleshooting_steps, R.string.deleted_items_cannot_be_restored_msg)
}
```
`ui/admin/guide/ui/EditorGuideScreen.kt`: `TopBarScaffold` плюс `LazyColumn` с раскрывающимися карточками `GuideSectionCard`. Шаги пронумерованы кружками, совет стоит внизу на фоне `tertiaryContainer`. Маршрут `admin/guide?section={section}` открывает нужный раздел сразу раскрытым, раздел читается из `SavedStateHandle` в `EditorGuideViewModel`.

```kotlin
@HiltViewModel
class EditorGuideViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val expandedSections = MutableStateFlow(
        setOfNotNull(savedStateHandle.get<String>(SECTION_ARG)?.let { name -> GuideSection.entries.firstOrNull { it.name == name } })
    )
    val uiState = expandedSections.map { EditorGuideUiState(sections = GuideSection.entries.toPersistentList(), expanded = it.toPersistentSet()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EditorGuideUiState())
    fun onUiEvent(event: EditorGuideUiEvent) {
        when (event) {
            is EditorGuideUiEvent.OnSectionClick -> expandedSections.update { expanded ->
                if (event.section in expanded) expanded - event.section else expanded + event.section
            }
        }
    }
    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        const val SECTION_ARG = "section"
    }
}
```
Тексты шагов берутся из `editor-guide.html` и хранятся как `<string-array>` в `values/strings.xml` и `values-be/strings.xml`. `Screen` получает `AdminGuideScreen`, `AdminGraph` — новый `composable`.

---

## 8. Иконки и строки

- Новые vector drawables (Material Symbols Rounded, как у существующих иконок): `check`, `palette`, `text_fields`, `language`, `notifications`, `star`, `mail`, `edit`, `logout`, `cloud_sync`, `menu_book`, `mic`, `help`, `google`.
- Каждая новая строка добавляется одновременно в `values/strings.xml` и `values-be/strings.xml`. Ключи называются по содержанию. Устаревшие ключи удаляются: `memorable_days_of_favorites` и старый `victory_day` с текстом «9 Мая — День Победы» меняются на новые.

---

## 9. Тесты

- `FavoritesRepositoryImplTest`: без аккаунта `toggle` пишет только локально; с аккаунтом пишет и локально, и в remote; `mergeLocalIntoAccount` отправляет весь локальный набор. Нужны новые фейки `FakeFavoritesLocalDataSource`, `FakeFavoritesRemoteDataSource` и `FakeGoogleAccountSource` в `testutil/`.
- `VisitorAccountRepositoryImplTest`: после `LINKED` и `SIGNED_IN` вызывается merge, после `FAILED` — нет.
- `AdminHomeViewModelTest`: счётчики контента берутся из фейковых репозиториев, а при ошибке становятся нулями.
- `EditorGuideViewModelTest`: раздел из `SavedStateHandle` раскрыт, `OnSectionClick` переключает его.

## Порядок коммитов
1. Нижняя панель показывает подпись только у активной вкладки.
2. Экран «Ещё»: группы, выпадающие списки, понятные напоминания.
3. Вход через Google и синхронизация избранного.
4. Новый главный экран админки и списки.
5. Памятка редактора в приложении.

## Проверка

```bash
./gradlew detektAll
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew installDebug
```

Ручные сценарии:
1. **Панель:** обычный вход, 4 вкладки. У активной видна подпись «О кладбище» целиком, у остальных только иконки, переключение анимируется. После входа администратора 5 вкладок, ничего не обрезается. Проверить на ru и be и с размером текста «Очень крупный».
2. **Настройки:** нажать «Тема», открывается список с галочкой у текущего варианта; выбор «Тёмная» сразу применяется. Так же проверить «Размер текста» и «Язык» (активити пересоздаётся, выбран «Беларуская»).
3. **Напоминания:** у обоих переключателей есть подзаголовки, нажатие на строку переключает значение.
4. **Google:** без входа добавить 2 ветеранов в избранное и отправить обращение. Войти через Google: избранное и обращение на месте (uid тот же). Удалить приложение, установить заново, войти тем же аккаунтом: избранное и «Мои обращения» вернулись. В консоли есть `OurMemory/Users/{uid}/favorites`.
5. **Выход:** после выхода из Google избранное показывает локальный набор, а новое обращение уходит от нового анонимного uid.
6. **Админка:** у главного экрана градиентная шапка, счётчики «Модерация» и «Обращения» подсвечены при значении больше 0, на плитках контента есть числа. Нажатие на плитку ведёт в нужный список.
7. **Памятка:** в списке ветеранов нажать «Как добавить ветерана?», открывается памятка с раскрытым разделом «Ветеран». Проверить тексты на be.
8. **Тёмная тема:** пройти все изменённые экраны, контраст плиток и баннера в порядке.
