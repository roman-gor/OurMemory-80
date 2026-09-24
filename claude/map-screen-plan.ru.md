# Экран карты кладбища с метками мест захоронения

## Контекст

В первой итерации появились модель `Burial` (узел `Burials` в Firebase, `Veteran.burialId`), `BurialsRepository` и мини-карта места на экране деталей. Следующий шаг из дорожной карты — полноэкранная интерактивная карта кладбища.

Что должно получиться:
- все места захоронения видны как метки, в плотных участках метки собираются в кластеры;
- тап по метке открывает карточку места: участок, ряд, место, фото, описание и список похороненных ветеранов с переходом в детали;
- фильтры «Герои Советского Союза» и «Деятели искусств», как на главном экране;
- кнопка «где я»: разрешение на геолокацию спрашивается только по нажатию;
- переключатель «схема / спутник»;
- вход в карту — кнопка в шапке главного экрана и «Показать на карте» из деталей (камера центрируется на месте, карточка сразу открыта).

Все нужные API есть в подключённом MapKit `4.19.0-navikit` (проверено по `classes.jar`):
- `ClusterizedPlacemarkCollection` и `ClusterListener`;
- `UserLocationLayer`;
- `LocationManager.requestSingleUpdate`;
- `MapType.HYBRID`.

Первое действие после одобрения — сохранить план в `claude/map-screen-plan.ru.md`.

## 1. Общие части карты выносятся в `ui/common`

**Почему.** Экраны деталей и карты оба используют `BurialUi` (номер участка, ряда и места) и один и тот же жизненный цикл `MapView`. Если оставить `BurialUi` в `ui/details/models`, фича карты будет зависеть от фичи деталей. Жизненный цикл `MapView` иначе придётся копировать. Координаты кладбища сейчас лежат приватными константами в `InfoScreen.kt`, а понадобятся и карте.

```text
ui/common/
├── models/
│   ├── BurialUi.kt            перенос из ui/details/models + поле id
│   └── CemeteryLocation.kt
└── ui/
    ├── MapPreview.kt          использует rememberMapViewWithLifecycle
    ├── MapViewLifecycle.kt
    └── PlotNumberText.kt      «Участок · ряд · место», вынос из BurialSection
```

```kotlin
data class BurialUi(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val section: String,
    val row: String,
    val place: String
) {
    val hasPlotNumber = section.isNotBlank() && row.isNotBlank() && place.isNotBlank()
}
```

```kotlin
object CemeteryLocation {
    const val LATITUDE = 53.908775
    const val LONGITUDE = 27.586246
}
```

```kotlin
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    DisposableEffect(mapView) {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        onDispose {
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }
    return mapView
}
```

## 2. Маршрут карты с необязательным `burialId`

**Почему.** Из деталей нужно открыть карту сразу на конкретном месте, а с главного экрана — без него. Необязательный query-аргумент покрывает оба случая одним маршрутом.

```kotlin
sealed class Screen(val route: String) {
    object HomeScreen : Screen("homescreen")
    object DetailScreen : Screen("detailscreen")
    object InfoScreen : Screen("infoscreen")
    object IntroScreen : Screen("introscreen")
    object MapScreen : Screen("mapscreen") {
        const val BURIAL_ID_ARG = "burialId"
        val pattern = "$route?$BURIAL_ID_ARG={$BURIAL_ID_ARG}"
        fun withBurial(burialId: String) = "$route?$BURIAL_ID_ARG=$burialId"
    }
}
```

```kotlin
composable(
    route = Screen.MapScreen.pattern,
    arguments = listOf(
        navArgument(Screen.MapScreen.BURIAL_ID_ARG) {
            type = NavType.StringType
            nullable = true
        }
    )
) {
    MapScreen(
        onBackClick = { navController.popBackStack() },
        onVeteranClick = { veteranId ->
            navController.navigate("${Screen.DetailScreen.route}/$veteranId")
        }
    )
}
```

## 3. UI-модели карты

**Почему.** Метке на карте нужны только координаты и id. Карточке места нужны тип, номер, фото, описание и список ветеранов. Разные модели не заставляют карту перерисовывать метки, когда открывается карточка.

```text
ui/map/models/
├── BurialMarkerUi.kt
├── BurialDetailsUi.kt
├── BurialType.kt
├── MapUiIntent.kt
├── MapUiState.kt
└── VeteranShortUi.kt
```

```kotlin
sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(
        val markers: ImmutableList<BurialMarkerUi>,
        val selectedBurial: BurialDetailsUi?,
        val focusedBurialId: String?,
        val checkedWar: Boolean,
        val checkedArt: Boolean
    ) : MapUiState
    data object Error : MapUiState
}
```

```kotlin
data class BurialMarkerUi(
    val id: String,
    val latitude: Double,
    val longitude: Double
)
```

```kotlin
data class BurialDetailsUi(
    val burial: BurialUi,
    val type: BurialType,
    val photo: String,
    val description: String,
    val veterans: ImmutableList<VeteranShortUi>
)
```

```kotlin
enum class BurialType(val value: String, @param:StringRes val nameRes: Int) {
    GRAVE("GRAVE", R.string.grave),
    MASS_GRAVE("MASS_GRAVE", R.string.mass_grave),
    MONUMENT("MONUMENT", R.string.monument);

    companion object {
        fun fromValue(value: String) = entries.firstOrNull { it.value == value } ?: GRAVE
    }
}
```

```kotlin
sealed interface MapUiIntent {
    data class OnMarkerClick(val burialId: String) : MapUiIntent
    data object OnSheetDismiss : MapUiIntent
    data class OnCheckedWarChange(val value: Boolean) : MapUiIntent
    data class OnCheckedArtChange(val value: Boolean) : MapUiIntent
}
```

## 4. `MapViewModel`

**Почему.** Места и ветераны грузятся один раз параллельно, а фильтры и выбранная метка меняются локально. Если комбинировать их в одну `combine`-цепочку, Firebase не перезапрашивается при каждом переключении фильтра. `burialId` из маршрута читается через `SavedStateHandle`, поэтому assisted-инъекция не нужна.

Правило видимости метки:
- место показывается, если среди его ветеранов есть хотя бы один подходящий под фильтр;
- место без ветеранов (памятник) видно всегда;
- места без координат (0, 0) пропускаются.

```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val burialsRepository: BurialsRepository,
    private val veteransRepository: VeteransRepository
) : ViewModel() {

    private val focusedBurialId: String? = savedStateHandle[Screen.MapScreen.BURIAL_ID_ARG]
    private val selectedBurialId = MutableStateFlow(focusedBurialId)
    private val checkedWarState = MutableStateFlow(true)
    private val checkedArtState = MutableStateFlow(true)

    val uiState = observeMapUiState().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        MapUiState.Loading
    )

    fun onUiIntent(intent: MapUiIntent) {
        when (intent) {
            is MapUiIntent.OnMarkerClick -> selectedBurialId.value = intent.burialId
            MapUiIntent.OnSheetDismiss -> selectedBurialId.value = null
            is MapUiIntent.OnCheckedWarChange -> checkedWarState.value = intent.value
            is MapUiIntent.OnCheckedArtChange -> checkedArtState.value = intent.value
        }
    }

    private fun observeMapUiState(): Flow<MapUiState> = combine(
        flow { emit(loadCemetery()) },
        checkedWarState,
        checkedArtState,
        selectedBurialId
    ) { cemetery, war, art, selectedId ->
        MapUiState.Success(
            markers = cemetery.visibleMarkers(war, art).toPersistentList(),
            selectedBurial = selectedId?.let(cemetery::detailsOf),
            focusedBurialId = focusedBurialId,
            checkedWar = war,
            checkedArt = art
        ) as MapUiState
    }.flowOn(Dispatchers.IO).catch { emit(MapUiState.Error) }

    private suspend fun loadCemetery() = coroutineScope {
        val burials = async { burialsRepository.getAllBurials() }
        val veterans = async { veteransRepository.getAllVeterans() }
        Cemetery(burials.await(), veterans.await().groupBy { it.burialId })
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val WAR_CATEGORY = "War"
        private const val ART_CATEGORY = "Art"
    }
}
```

`Cemetery` — приватный вспомогательный `data class` в том же файле с методами `visibleMarkers(war, art)` и `detailsOf(id)`. Это допустимое исключение из правила «один тип на файл»: тип используется только этой ViewModel.

## 5. Компонент карты с кластерами

**Почему.** MapKit хранит слушатели нажатий (`MapObjectTapListener`, `ClusterListener`, `ClusterTapListener`, `LocationListener`) через **слабые ссылки**. Если создать слушатель лямбдой прямо при вызове, сборщик мусора его удалит, и тапы по меткам перестанут работать. Поэтому все слушатели создаются в `remember` и живут столько же, сколько composable. Метки пересоздаются только при изменении списка `markers`: `clear()` + `addPlacemark()` + `clusterPlacemarks()`.

```kotlin
@Composable
fun BurialsMap(
    markers: ImmutableList<BurialMarkerUi>,
    focusedBurialId: String?,
    mapType: MapType,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val markerIcon = remember { ImageProvider.fromResource(context, R.drawable.ic_marker) }
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)

    val markerTapListener = remember {
        MapObjectTapListener { mapObject, _ ->
            (mapObject.userData as? String)?.let(currentOnMarkerClick)
            true
        }
    }
    val clusterTapListener = remember {
        ClusterTapListener { cluster ->
            mapView.mapWindow.map.move(
                CameraPosition(cluster.appearance.geometry, mapView.mapWindow.map.cameraPosition.zoom + CLUSTER_ZOOM_STEP, 0f, 0f)
            )
            true
        }
    }
    val clusterListener = remember {
        ClusterListener { cluster ->
            cluster.appearance.setIcon(ClusterImageProvider(context, cluster.size))
            cluster.addClusterTapListener(clusterTapListener)
        }
    }
    val collection = remember { mapView.mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(clusterListener) }

    LaunchedEffect(mapType) { mapView.mapWindow.map.mapType = mapType }

    LaunchedEffect(markers) {
        collection.clear()
        markers.forEach { marker ->
            collection.addPlacemark().apply {
                geometry = Point(marker.latitude, marker.longitude)
                setIcon(markerIcon)
                userData = marker.id
                addTapListener(markerTapListener)
            }
        }
        collection.clusterPlacemarks(CLUSTER_RADIUS, CLUSTER_MIN_ZOOM)
    }

    LaunchedEffect(Unit) {
        val focused = markers.firstOrNull { it.id == focusedBurialId }
        val target = focused?.let { Point(it.latitude, it.longitude) }
            ?: Point(CemeteryLocation.LATITUDE, CemeteryLocation.LONGITUDE)
        mapView.mapWindow.map.move(
            CameraPosition(target, if (focused != null) FOCUSED_ZOOM else CEMETERY_ZOOM, 0f, 0f)
        )
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
```

Значок кластера — `ClusterImageProvider` (свой файл): бордовый круг с числом, нарисованный на `Canvas` в `Bitmap`; `getId()` возвращает `"cluster_$size"` для кеширования. Константы — `CLUSTER_RADIUS = 60.0`, `CLUSTER_MIN_ZOOM = 19`, `CLUSTER_ZOOM_STEP = 2f`, `CEMETERY_ZOOM = 16f`, `FOCUSED_ZOOM = 19f`.

## 6. «Где я» и разрешение на геолокацию

**Почему.** На кладбище геолокация — основной способ сориентироваться. При этом просить разрешение на старте экрана навязчиво: карта полезна и без него. Поэтому разрешение запрашивается только по кнопке. Если пользователь отказал, показывается Snackbar с объяснением, а карта продолжает работать.

В `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

```kotlin
@Composable
fun rememberMyLocationAction(mapView: MapView, onPermissionDenied: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val userLocationLayer = remember {
        MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
    }
    val locationManager = remember { MapKitFactory.getInstance().createLocationManager() }
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                mapView.mapWindow.map.move(CameraPosition(location.position, MY_LOCATION_ZOOM, 0f, 0f))
            }

            override fun onLocationStatusUpdated(status: LocationStatus) = Unit
        }
    }
    val showMyLocation = {
        userLocationLayer.isVisible = true
        locationManager.requestSingleUpdate(locationListener)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) showMyLocation() else onPermissionDenied()
    }
    return {
        if (context.hasLocationPermission()) {
            showMyLocation()
        } else {
            permissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }
    }
}
```

Если разрешение уже есть, слой «где я» включается сразу при открытии карты.

## 7. Экран карты и карточка места

**Почему.** Карта занимает весь экран, остальное лежит поверх: фильтры сверху, кнопки справа внизу. Карточка — `ModalBottomSheet`, как у наград в деталях. Так пользователь видит место на карте и информацию о нём одновременно.

```text
ui/map/ui/
├── MapScreen.kt              Scaffold + TopAppBar(← «Карта кладбища») + Box(карта, фильтры, кнопки)
├── BurialsMap.kt
├── ClusterImageProvider.kt
├── MyLocationAction.kt       rememberMyLocationAction
├── MapFilterChips.kt         FilterChip × 2, строки heroUSSR / art
├── MapControls.kt            SmallFloatingActionButton: схема/спутник, «где я»
└── BurialBottomSheet.kt      тип, PlotNumberText, фото, описание, список ветеранов
```

```kotlin
@Composable
fun BurialBottomSheet(
    details: BurialDetailsUi,
    onVeteranClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp)) {
            item {
                Text(stringResource(details.type.nameRes), style = MaterialTheme.typography.titleLarge)
                PlotNumberText(burial = details.burial)
            }
            if (details.photo.isNotBlank()) {
                item { AsyncImage(model = details.photo, contentDescription = null) }
            }
            if (details.description.isNotBlank()) {
                item { Text(details.description, style = MaterialTheme.typography.bodyLarge) }
            }
            items(details.veterans, key = { it.id }) { veteran ->
                VeteranRow(veteran = veteran, onClick = { onVeteranClick(veteran.id) })
            }
        }
    }
}
```

Тип карты (`MapType.MAP` / `MapType.HYBRID`) — UI-состояние экрана в `rememberSaveable`, во ViewModel не попадает.

## 8. Входы в карту

**Почему.** Карта должна открываться из двух мест. На главном экране — кнопка в шапке, в том же стиле, что «памятник» и фильтр. В деталях место захоронения ведёт на полную карту, где видно окружение и работает «где я».

Главный экран: в `HeaderData` добавляется `navigateToMapScreen`, в `Header` — ещё одна квадратная кнопка слева от «памятника». Иконка — новый векторный `res/drawable/map.xml`.

```kotlin
Button(
    onClick = { data.navigateToMapScreen() },
    modifier = Modifier.padding(start = 8.dp).aspectRatio(1f).weight(1f),
    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.dark_red)),
    contentPadding = PaddingValues(0.dp)
) {
    Icon(
        painter = painterResource(R.drawable.map),
        contentDescription = stringResource(R.string.cemetery_map),
        tint = colorResource(R.color.white),
        modifier = Modifier.padding(8.dp)
    )
}
```

Детали: `BurialSection` получает `onShowOnMapClick`, под мини-картой появляется `OutlinedButton` «Показать на карте». Прокидка: `DetailsScreen(onShowOnMapClick: (String) -> Unit)` → `AppNavigation`:

```kotlin
DetailsScreen(
    detailsViewModel = detailsViewModel,
    onBackClick = { navController.popBackStack() },
    onShowOnMapClick = { burialId -> navController.navigate(Screen.MapScreen.withBurial(burialId)) }
)
```

## 9. Строки

**Почему.** Все надписи берутся из ресурсов, в `values/` и `values-be/`. Фильтры переиспользуют существующие `heroUSSR` и `art`.

```xml
<string name="cemetery_map">Карта кладбища</string>
<string name="show_on_map">Показать на карте</string>
<string name="my_location">Моё местоположение</string>
<string name="satellite">Спутник</string>
<string name="scheme">Схема</string>
<string name="grave">Могила</string>
<string name="mass_grave">Братская могила</string>
<string name="monument">Памятник</string>
<string name="allow_location_access_msg">Разрешите доступ к геолокации, чтобы увидеть себя на карте</string>
```

## Критичные файлы

- новые: `ui/map/{models,ui,viewmodels}/*`, `ui/common/models/{BurialUi,CemeteryLocation}.kt`, `ui/common/ui/{MapViewLifecycle,PlotNumberText}.kt`, `res/drawable/map.xml`
- `ui/common/ui/MapPreview.kt` — переход на `rememberMapViewWithLifecycle`
- `ui/details/models/BurialUi.kt` → `ui/common/models/`; `ui/details/ui/BurialSection.kt`, `DetailsScreen.kt` — кнопка «Показать на карте»
- `ui/screens/InfoScreen.kt` — константы → `CemeteryLocation`
- `ui/screens/HomeScreen.kt` — кнопка карты в шапке
- `domain/models/Screen.kt`, `ui/AppNavigation.kt` — маршрут
- `AndroidManifest.xml` — разрешения на геолокацию
- `res/values*/strings.xml`

## Проверка

1. `./gradlew assembleDebug` и `./gradlew detektAll` проходят.
2. Тестовые данные: подготовлю `claude/burials-sample.json` на 6–8 мест вокруг `CemeteryLocation`:
   - несколько мест рядом друг с другом, чтобы проверить кластер;
   - одна братская могила с двумя ветеранами;
   - один памятник без ветеранов.

   Его можно импортировать в Firebase Console (узел `Burials`) и проставить `burialId` ветеранам. Нужно, чтобы правила безопасности разрешали чтение `Burials`.
3. Ручные сценарии на устройстве:
   - Главный экран → кнопка карты: камера на кладбище, метки и кластеры видны. Тап по кластеру приближает, кластер распадается.
   - Тап по метке: карточка с типом, «Участок · ряд · место», фото, описанием, ветеранами. Тап по ветерану открывает детали, «назад» возвращает на карту.
   - Братская могила: в карточке два ветерана. Памятник: карточка без ветеранов, метка видна при любых фильтрах.
   - Фильтры: отключить «Деятели искусств» — метки только искусства пропадают. Отключить оба — видны только памятники.
   - Детали ветерана с `burialId` → «Показать на карте»: камера на месте, карточка открыта.
   - «Где я» без разрешения → системный диалог. «Разрешить» → камера на пользователе, виден значок. «Запретить» → Snackbar, карта работает.
   - Переключатель схема/спутник меняет подложку и переживает поворот экрана и возврат с деталей.
   - Много тапов по меткам после прокрутки и масштабирования: тапы продолжают срабатывать (проверка сильных ссылок на слушатели).
   - Белорусский язык: новые надписи переведены.
