# Обновление дизайна Android: идеи из iOS-версии в духе Material 3

## Контекст

Нужно освежить дизайн Android-приложения, взяв хорошее из `~/Personal/OurMemory-ios`. iOS-версия сделана по Human Interface Guidelines: inset-grouped списки, large titles, frosted-панели, segmented control. Если перенести это на Android буквально, получится чужой интерфейс, который расходится с Material. Поэтому переносим не вид, а **идеи**, и каждую выражаем компонентами Material 3.

Большая часть iOS-версии портирована с Android: туры, аудио, галерея, герой-шапка на обеих платформах почти одинаковые. Реальных отличий немного, и по итогам обсуждения берём:

- **Из iOS:** живая свеча и отклик на «избранное», «Поделиться ветераном», шторка захоронения на полэкрана с живой картой, пустые и итоговые экраны, статусы обращений.
- **Сверх iOS, по Material:** скелетоны загрузки, «потянуть для обновления», «смахнуть из избранного».
- **Не трогаем:** плавающую панель вкладок, круглую кнопку «назад» и группы настроек. Они аккуратные и работают, а переделка большая.

Одно уточнение к «потянуть для обновления». «Мои обращения» и «Избранное» подписаны на живые данные (`observeMyRequests`, `observeFavorites`) и обновляются сами. Жест там ничего бы не делал, поэтому добавляю его только на список ветеранов: он кэшируется на весь процесс (`VeteransRepository.invalidate()`).

Работа идёт блоками, после каждого блока отдельный коммит.

---

## Блок 1. Карточка ветерана

### 1.1 Живая свеча

**Почему:** сейчас `CandleCard` (`ui/details/ui/CandleCard.kt`) просто перекрашивает кружок: нажатие никак не ощущается, число меняется скачком. Для мемориального жеста важен отклик: короткая вибрация, мягкое «дыхание» пламени после зажигания и прокрутка счётчика.

```kotlin
@Composable
fun CandleCard(
    candleState: CandleState,
    onLightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLit = candleState.isLitToday
    val haptics = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(enabled = !isLit) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onLightClick()
            }
            .padding(16.dp)
    ) {
        CandleFlame(isLit = isLit)
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(
                text = stringResource(if (isLit) R.string.candle_is_lit else R.string.light_a_candle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AnimatedContent(
                targetState = candleState.count,
                transitionSpec = { slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut() },
                label = "candlesCount"
            ) { count ->
                Text(
                    text = pluralStringResource(R.plurals.candles_count, count.toPluralQuantity(), count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

Пламя выносится в отдельный файл `ui/details/ui/CandleFlame.kt`. Цвет кружка меняется через `animateColorAsState`, а горящее пламя «дышит» через `rememberInfiniteTransition`, так же как в `MemoryBanner`:

```kotlin
@Composable
fun CandleFlame(isLit: Boolean, modifier: Modifier = Modifier) {
    val containerColor by animateColorAsState(
        if (isLit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
        label = "flameContainer"
    )
    val breath by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue = 1f,
        targetValue = if (isLit) BREATH_SCALE else 1f,
        animationSpec = infiniteRepeatable(tween(BREATH_MILLIS), RepeatMode.Reverse),
        label = "flameScale"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(52.dp).clip(CircleShape).background(containerColor)
    ) {
        Icon(
            painter = painterResource(R.drawable.flame),
            contentDescription = null,
            tint = if (isLit) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer { scaleX = breath; scaleY = breath }
        )
    }
}

private const val BREATH_SCALE = 1.12f
private const val BREATH_MILLIS = 1200
```

`count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()` уже встречается в текущем коде. Выношу его в приватное расширение `Long.toPluralQuantity()`, чтобы не повторять.

### 1.2 Отклик на «избранное»

**Почему:** сердечко в `FloatingTopBar` меняет иконку без анимации и вибрации, и нажатие легко не заметить.

Кнопка выносится в `ui/details/ui/FavoriteButton.kt`: при добавлении короткий «пружинный» скачок масштаба и вибрация.

```kotlin
@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }
    LaunchedEffect(isFavorite) {
        if (isFavorite) {
            scale.animateTo(POP_SCALE, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    CircleIconButton(
        painter = painterResource(if (isFavorite) R.drawable.favorite else R.drawable.favorite_border),
        contentDescription = stringResource(if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites),
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onClick()
        },
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
    )
}

private const val POP_SCALE = 1.25f
```

У этого решения есть побочный эффект: `LaunchedEffect(isFavorite)` сработает и при первом показе карточки, если ветеран уже в избранном. Чтобы этого не было, анимацию запускаю только после изменения значения: храню предыдущее в `remember` и сравниваю с ним.

### 1.3 «Поделиться ветераном»

**Почему:** на Android сейчас нигде нельзя поделиться. На iOS в шапке карточки есть кнопка «Поделиться», а у приложения уже есть веб-страница и ссылка `https://chatroom-85fb8.web.app/veteran/{id}`, которые открывают карточку.

`ui/common/models/VeteranLink.kt` получает функцию сборки ссылки:

```kotlin
fun forId(veteranId: String) = "$BASE_URL$PATH_SEPARATOR$veteranId"
```

`ui/details/ui/ShareVeteranAction.kt` открывает системное окно «Поделиться»:

```kotlin
@Composable
fun rememberShareVeteranAction(veteranId: String, veteranName: String): () -> Unit {
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.share)
    val shareText = stringResource(R.string.remember_veteran_msg, veteranName, VeteranLink.forId(veteranId))
    return remember(shareText) {
        {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = PLAIN_TEXT
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        }
    }
}

private const val PLAIN_TEXT = "text/plain"
```

В `DetailsScreen` в `actions` у `FloatingTopBar` появляется вторая `CircleIconButton` с новой иконкой `res/drawable/share.xml` (Material Symbols «share»). Строки на ru и be:

```xml
<string name="share">Поделиться</string>
<string name="remember_veteran_msg">%1$s — помним. Карточка ветерана: %2$s</string>
```

```xml
<string name="share">Падзяліцца</string>
<string name="remember_veteran_msg">%1$s — памятаем. Картка ветэрана: %2$s</string>
```

---

## Блок 2. Шторка захоронения на полэкрана

**Почему:** `BurialBottomSheet` сейчас сделана как `ModalBottomSheet`: карту затемняет подложка, и пока шторка открыта, с картой ничего нельзя сделать. У могилы нужно одновременно видеть описание и карту, чтобы понимать, куда идти. На iOS шторка встаёт на половину высоты, и карта над ней остаётся живой. В Material для этого есть **стандартная** (немодальная) шторка, `BottomSheetScaffold`. После прошлого исправления карта рисуется через TextureView, так что шторка поверх неё анимируется корректно.

`MapContent` (`ui/map/ui/MapScreen.kt`) оборачивается в `BottomSheetScaffold`. Показ шторки зависит от `state.selectedBurial`: когда захоронение выбрано, шторка встаёт на половину высоты. Когда её смахивают вниз, уходит `OnSheetDismiss`:

```kotlin
val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Hidden, skipHiddenState = false)
val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

LaunchedEffect(state.selectedBurial?.burial?.id) {
    if (state.selectedBurial != null) sheetState.partialExpand() else sheetState.hide()
}
LaunchedEffect(sheetState) {
    snapshotFlow { sheetState.currentValue }
        .filter { it == SheetValue.Hidden }
        .collect { onUiIntent(MapUiIntent.OnSheetDismiss) }
}

BottomSheetScaffold(
    scaffoldState = scaffoldState,
    sheetPeekHeight = SHEET_PEEK_HEIGHT,
    sheetContent = {
        state.selectedBurial?.let { details ->
            BurialSheetContent(
                details = details,
                bottomPadding = LocalBottomBarInset.current,
                onVeteranClick = onVeteranClick
            )
        }
    }
) {
    Box(modifier = Modifier.fillMaxSize()) { /* карта, фильтры, FAB, MapControls как сейчас */ }
}
```

`BurialBottomSheet` превращается в `BurialSheetContent`: это та же `LazyColumn`, но без обёртки `ModalBottomSheet`, с нижним отступом `bottomPadding` плюс `navigationBars`, чтобы плавающая панель вкладок не закрывала последние строки. Шторка экскурсий (`ToursSheet`) остаётся модальной: это выбор, который открывает другой экран, и живая карта в этот момент не нужна.

`SHEET_PEEK_HEIGHT` — это `private val` в `MapScreen.kt`. Его значение подбираю так, чтобы были видны заголовок, номер участка и фото. Главное — проверить, что при нажатии на другой маркер шторка переключается на новое захоронение, а не закрывается.

---

## Блок 3. Пустые и итоговые экраны, статусы обращений

### 3.1 Общий компонент пустого состояния

**Почему:** сейчас пустые экраны — это голая строка серого текста в верхней части списка («Избранное», «Мои обращения», «Ветераны»). В Material пустое состояние показывают крупной иконкой и текстом по центру, чтобы было понятно, что экран не завис, а данных просто нет.

`ui/common/ui/EmptyContent.kt`:

```kotlin
@Composable
fun EmptyContent(
    @DrawableRes iconRes: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(ICON_SIZE)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private val ICON_SIZE = 56.dp
```

Где применяется:
- **«Избранное»:** `favorite_border` + `no_favorites_yet_msg`.
- **«Мои обращения»:** `mail` + `no_requests_yet_msg`.
- **«Ветераны»:** два разных случая. Сейчас оба показывают «Выберите категорию», и это неверно, когда фильтры включены, а поиск ничего не нашёл:

```kotlin
if (state.veterans.isEmpty()) {
    item {
        if (state.checkedWar || state.checkedArt) {
            EmptyContent(iconRes = R.drawable.search_icon, message = stringResource(R.string.nothing_found_msg, state.search))
        } else {
            EmptyContent(iconRes = R.drawable.star, message = stringResource(R.string.chooseCategory))
        }
    }
}
```

```xml
<string name="nothing_found_msg">По запросу «%1$s» никого не нашли. Проверьте написание фамилии</string>
```

```xml
<string name="nothing_found_msg">Па запыце «%1$s» нікога не знайшлі. Праверце напісанне прозвішча</string>
```

### 3.2 Итоговый экран после отправки

**Почему:** `SentContent` в `SubmissionScreen` и `FeedbackSentContent` в `FeedbackScreen` — это одинаковые экраны из заголовка и кнопки. Их код продублирован, и на них нет знака успешной отправки.

Общий `ui/common/ui/SentContent.kt`: галочка в кружке `primaryContainer` появляется «пружиной» (`AnimatedVisibility` + `scaleIn(spring(...))`), под ней сообщение и кнопка «Готово». Оба экрана вызывают его со своим текстом:

```kotlin
@Composable
fun SentContent(message: String, onDoneClick: () -> Unit, modifier: Modifier = Modifier) {
    var isShown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isShown = true }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize().padding(32.dp)
    ) {
        AnimatedVisibility(
            visible = isShown,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(CHECK_CIRCLE_SIZE).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(CHECK_ICON_SIZE)
                )
            }
        }
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
        Button(onClick = onDoneClick, modifier = Modifier.padding(top = 24.dp)) {
            Text(text = stringResource(R.string.done))
        }
    }
}

private val CHECK_CIRCLE_SIZE = 88.dp
private val CHECK_ICON_SIZE = 44.dp
```

### 3.3 Статусы обращений цветной меткой

**Почему:** сейчас статус в `MyRequestCard` — это цветной текст: «На рассмотрении» и «Одобрено» окрашены одинаково в `primary`, и различить их трудно. Ответ кладбища сливается с текстом самого обращения.

Статус становится меткой на цветной подложке. Цвет у каждого статуса свой и берётся из ролей цветовой схемы:

| Статус | Подложка / текст |
|---|---|
| `IN_REVIEW` | `surfaceContainerHighest` / `onSurfaceVariant` |
| `APPROVED`, `REVIEWED` | `tertiaryContainer` / `onTertiaryContainer` (зелёный) |
| `REJECTED` | `errorContainer` / `onErrorContainer` |

В теме сейчас нет роли `tertiary`, поэтому Compose подставляет свой фиолетовый по умолчанию. Добавляю в `Color.kt` приглушённый зелёный и прописываю его в обе схемы в `Theme.kt`:

```kotlin
val MemoryGreenContainer = Color(0xFFDCEBD8)
val MemoryOnGreenContainer = Color(0xFF0F2A12)
val MemoryGreenContainerDark = Color(0xFF28402A)
val MemoryOnGreenContainerDark = Color(0xFFCDE8C8)
```

```kotlin
tertiaryContainer = MemoryGreenContainer,
onTertiaryContainer = MemoryOnGreenContainer,
```

Метка — `ui/myrequests/ui/RequestStatusLabel.kt`:

```kotlin
@Composable
fun RequestStatusLabel(status: RequestStatus) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        RequestStatus.IN_REVIEW -> colors.surfaceContainerHighest to colors.onSurfaceVariant
        RequestStatus.APPROVED, RequestStatus.REVIEWED -> colors.tertiaryContainer to colors.onTertiaryContainer
        RequestStatus.REJECTED -> colors.errorContainer to colors.onErrorContainer
    }
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = stringResource(status.labelRes),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
```

Ответ кладбища показывается отдельным блоком на `surfaceContainer` со скруглением 12dp: подпись «Ответ» (`labelMedium`, `primary`) и текст ответа под ней. Строку `reply_from_cemetery` с `%1$s` заменяю на `reply` («Ответ» / «Адказ»). Если старая строка больше нигде не используется, удаляю её.

---

## Блок 4. Скелетоны, обновление списка, смахивание из избранного

### 4.1 Скелетоны загрузки

**Почему:** сейчас список ветеранов и карточка ветерана на время загрузки показывают спиннер посреди пустого экрана, и после загрузки интерфейс резко «впрыгивает». Material советует показывать заглушки в форме будущего содержимого: пользователь сразу видит структуру экрана, и появление данных выглядит спокойнее. Сторонняя библиотека для этого не нужна.

`ui/common/ui/Placeholder.kt` — модификатор с мягкой пульсацией:

```kotlin
@Composable
fun Modifier.placeholder(shape: Shape = RoundedCornerShape(8.dp)): Modifier {
    val alpha by rememberInfiniteTransition(label = "placeholder").animateFloat(
        initialValue = MIN_ALPHA,
        targetValue = MAX_ALPHA,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
        label = "placeholderAlpha"
    )
    return this
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
}

private const val MIN_ALPHA = 0.4f
private const val MAX_ALPHA = 1f
private const val PULSE_MILLIS = 800
```

- `ui/home/ui/HomeLoadingContent.kt`: заголовок, поле поиска и шесть строк `VeteranItem`, повторяющих размеры настоящих строк: портрет, две строки текста и абзац.
- `ui/details/ui/DetailsLoadingContent.kt`: блок героя с тем же соотношением сторон (0.85), под ним заглушки карточки свечи и трёх строк текста.

В `MainScreen` и `DetailsScreen` вместо `LoadingContent` показывается соответствующий скелетон. Остальные экраны оставляю со спиннером: они загружаются быстро или открываются редко.

### 4.2 «Потянуть для обновления» на списке ветеранов

**Почему:** `VeteransRepositoryImpl` загружает ветеранов один раз за процесс. Если админ добавил ветерана, посетитель увидит его только после перезапуска приложения: это указано и в руководстве администратора, в пункте «Посетитель не видит изменения». Жест «потянуть вниз» — стандартный для Android способ получить свежие данные.

`HomeUiIntent` получает `data object OnRefresh`, а `HomeUiState.Success` — поле `isRefreshing`. `HomeViewModel`:

```kotlin
private val isRefreshingState = MutableStateFlow(false)
private val reloadState = MutableStateFlow(0)

private fun refresh() {
    if (isRefreshingState.value) return
    viewModelScope.launch {
        isRefreshingState.value = true
        runCatching { repository.invalidate() }
            .onFailure { Log.e(LOG_TAG, "Failed to refresh veterans", it) }
        reloadState.update { it + 1 }
        isRefreshingState.value = false
    }
}
```

`reloadState` и `isRefreshingState` добавляются в `combine`, поэтому после `invalidate()` список перечитывается. Экран оборачивает `LazyColumn` в `PullToRefreshBox`:

```kotlin
PullToRefreshBox(
    isRefreshing = state.isRefreshing,
    onRefresh = { onUiIntent(HomeUiIntent.OnRefresh) },
    modifier = Modifier.fillMaxSize()
) {
    LazyColumn(...) { ... }
}
```

Индикатор нужно сдвинуть вниз на высоту статус-бара, потому что экран edge-to-edge: `indicator = { PullToRefreshDefaults.Indicator(..., modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()) }`. В тест `HomeViewModelTest` (если его нет — создаю по образцу `MapViewModelTest`) добавляю проверку, что после `OnRefresh` у `FakeVeteransRepository` вызван `invalidate()` и список перечитан.

### 4.3 Смахнуть из избранного

**Почему:** сейчас убрать ветерана из избранного можно только так: открыть его карточку и нажать сердечко. Смахивание строки с отменой через Snackbar — стандартный паттерн Material для списков.

`ui/favorites/models/FavoritesUiIntent.kt`:

```kotlin
sealed interface FavoritesUiIntent {
    data class OnRemove(val veteranId: String) : FavoritesUiIntent
    data class OnUndoRemove(val veteranId: String) : FavoritesUiIntent
}
```

`FavoritesViewModel` получает `favoritesRepository` как `private val` и обработчик. У `FavoritesRepository` есть только `toggle`, поэтому обе команды вызывают его. `toggle` сам читает текущее значение, так что повторное нажатие «Отменить» ничего не сломает:

```kotlin
fun onUiIntent(intent: FavoritesUiIntent) {
    when (intent) {
        is FavoritesUiIntent.OnRemove -> viewModelScope.launch { favoritesRepository.toggle(intent.veteranId) }
        is FavoritesUiIntent.OnUndoRemove -> viewModelScope.launch { favoritesRepository.toggle(intent.veteranId) }
    }
}
```

Строка `FavoriteRow` оборачивается в `SwipeToDismissBox`. Под ней виден красный фон `errorContainer` с иконкой `remove`, смахивать можно только справа налево. После смахивания экран показывает Snackbar:

```kotlin
val dismissState = rememberSwipeToDismissBoxState()
SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = false,
    onDismiss = {
        onUiIntent(FavoritesUiIntent.OnRemove(item.id))
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = removedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) onUiIntent(FavoritesUiIntent.OnUndoRemove(item.id))
        }
    },
    backgroundContent = { RemoveBackground() }
) {
    FavoriteRow(item = item, onClick = { onVeteranClick(item.id) })
}
```

Точную сигнатуру `SwipeToDismissBox` для BOM `2026.08.00` сверяю при реализации: в последних версиях `confirmValueChange` заменён на `onDismiss`. Строки добавляются в ru и be:

```xml
<string name="removed_from_favorites">Удалено из избранного</string>
<string name="undo">Отменить</string>
```

```xml
<string name="removed_from_favorites">Выдалена з абранага</string>
<string name="undo">Адмяніць</string>
```

Список получает `Modifier.animateItem()`, чтобы соседние строки плавно сдвигались на место удалённой.

---

## Что сознательно не переносим

- iOS-списки inset-grouped, large titles, frosted-панели и segmented control: у Material для этого свои компоненты, а текущие экраны уже решают эти задачи.
- Размытие фона на интро (`blur`) работает только с Android 12. Выигрыш маленький, а на старых устройствах получится другой вид.
- Серифный шрифт названия приложения: в проекте уже лежит `inriaFont`. Решение по нему — отдельный разговор о бренде, в этот план не включаю.

## Файлы (основные)

- `ui/details/ui/`: `CandleCard.kt`, `CandleFlame.kt` (новый), `FavoriteButton.kt` (новый), `ShareVeteranAction.kt` (новый), `DetailsScreen.kt`, `DetailsLoadingContent.kt` (новый)
- `ui/map/ui/`: `MapScreen.kt`, `BurialBottomSheet.kt` → `BurialSheetContent.kt`
- `ui/common/ui/`: `EmptyContent.kt`, `SentContent.kt`, `Placeholder.kt` (новые)
- `ui/common/models/VeteranLink.kt`
- `ui/home/`: `HomeScreen.kt`, `HomeLoadingContent.kt` (новый), `HomeViewModel.kt`, `HomeUiState.kt`, `HomeUiIntent.kt`
- `ui/favorites/`: `FavoritesScreen.kt`, `FavoritesViewModel.kt`, `FavoritesUiIntent.kt` (новый)
- `ui/myrequests/ui/`: `MyRequestCard.kt`, `RequestStatusLabel.kt` (новый), `MyRequestsScreen.kt`
- `ui/submission/ui/SubmissionScreen.kt`, `ui/feedback/ui/FeedbackScreen.kt`
- `ui/theme/Color.kt`, `ui/theme/Theme.kt`
- `res/drawable/share.xml` (новый), `res/values/strings.xml`, `res/values-be/strings.xml`

## Проверка

```bash
./gradlew testDebugUnitTest
./gradlew detektAll
./gradlew assembleDebug
./gradlew installDebug
```

Вручную, в светлой и тёмной теме:
1. **Карточка ветерана:**
   - нажатие на свечу даёт вибрацию, кружок плавно меняет цвет, пламя «дышит», счётчик прокручивается;
   - повторно зажечь свечу нельзя;
   - сердечко «подпрыгивает» при добавлении и не подпрыгивает при открытии карточки уже избранного ветерана;
   - «Поделиться» открывает системное окно; ссылка из Telegram открывает эту карточку.
2. **Карта:**
   - нажатие на маркер поднимает шторку на полэкрана, а карта над ней двигается и масштабируется;
   - нажатие на другой маркер переключает шторку;
   - смахивание вниз закрывает шторку;
   - на вкладке последние строки шторки не прячутся под панелью вкладок;
   - «Экскурсии» по-прежнему открываются модальной шторкой.
3. **Ветераны:**
   - при медленном интернете (эмулятор, 2G) видны скелетоны, а не спиннер;
   - поиск «ььь» показывает «никого не нашли», оба фильтра выключены — «Выберите категорию»;
   - потянуть вниз: индикатор ниже статус-бара, ветеран, добавленный из админки, появляется без перезапуска.
4. **Избранное:**
   - смахнуть строку влево — она уходит, остальные плавно сдвигаются, появляется Snackbar; «Отменить» возвращает строку;
   - пустое избранное показывает иконку и текст по центру.
5. **Мои обращения:** у статусов разные цветные метки; ответ кладбища — отдельный блок; пустой список показывает иконку.
6. **Отправка материала и обратной связи:** после отправки «пружиной» появляется галочка, «Готово» закрывает экран.
7. Переключить язык на белорусский и пройти пункты 1–6: новые строки переведены.
