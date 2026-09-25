# Убрать «прыжок» карты при переходе на другой экран

## Контекст

Пользователь уходит с экрана карты: открывает карточку ветерана из шторки захоронения, экскурсию, переключает вкладку. Примерно на секунду карта и тени от кнопок остаются на экране поверх нового экрана, а потом резко пропадают.

### Почему это происходит

1. `NavHost` (navigation-compose 2.10.0) по умолчанию меняет экраны плавным переходом: старый экран гаснет через `fadeOut`, новый появляется через `fadeIn`, и это длится около 700 мс.
2. `rememberMapViewWithLifecycle` (`ui/common/ui/MapViewLifecycle.kt`) создаёт карту так: `MapView(context)`. Без атрибутов MapKit 4.19 выбирает `PlatformGLSurfaceView`, то есть **SurfaceView**. Я проверил это по байткоду `PlatformViewFactory.getPlatformView`: `PlatformGLTextureView` создаётся только при атрибуте `MOVABLE`.
3. SurfaceView рисуется в отдельном окне под интерфейсом, в «дыре», которую для него прорезает Compose. Прозрачность и анимации Compose на это окно не действуют. Весь экран карты плавно гаснет, а сама карта остаётся непрозрачной до конца перехода и потом исчезает за один кадр. Отсюда и «прыжок». Вместе с картой зависают тени кнопок: FAB «Экскурсии» и `MapControls` рисуются поверх этой «дыры».

Та же `rememberMapViewWithLifecycle` используется в `MapPreview`, то есть в мини-картах на карточке ветерана (`details/ui/BurialSection.kt`) и на «О приложении» (`info/ui/LocationSection.kt`). У них при переходах тот же дефект.

## Решение: рисовать карту через TextureView

У MapKit есть XML-атрибут `app:movable="true"` (styleable `PlatformView`). С ним карта рисуется в `PlatformGLTextureView`, как обычная View: она гаснет, двигается и обрезается вместе с экраном. Атрибут читается только из `AttributeSet` (`PlatformViewFactory.convertAttributeSet`), в коде его не задать, поэтому карту нужно создавать из разметки.

Правка одна, в общей функции, и она чинит и основную карту, и мини-карты.

### 1. Разметка карты

**Почему:** у `MapView` нет конструктора или сеттера для movable-режима, его можно задать только атрибутом разметки.

`app/src/main/res/layout/movable_map_view.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.yandex.mapkit.mapview.MapView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:movable="true" />
```

### 2. Создание карты из разметки

**Почему:** `MapView(context)` даёт SurfaceView, и Compose-анимации на неё не действуют.

`ui/common/ui/MapViewLifecycle.kt`:

```kotlin
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { context.inflateMovableMapView() }
    ...
}

private fun Context.inflateMovableMapView() =
    LayoutInflater.from(this).inflate(R.layout.movable_map_view, null, false) as MapView
```

Остальной код функции (`start`/`stop`, ночной режим) остаётся как есть.

### Цена решения

TextureView немного медленнее SurfaceView и расходует чуть больше памяти: кадр копируется в общий слой интерфейса. Для карты одного кладбища с кластеризацией это незаметно. Сейчас SurfaceView всё равно не даёт анимировать карту и мешает `ModalBottomSheet` поверх неё. Альтернатива — отключить анимацию перехода для маршрутов с картой. Она хуже: прыжок заменится резкой сменой экрана, а мини-карты на карточке ветерана так и останутся зависать.

## Файлы

- `app/src/main/res/layout/movable_map_view.xml` — новый
- `app/src/main/java/com/gorman/ourmemoryapp/ui/common/ui/MapViewLifecycle.kt`

## Проверка

```bash
./gradlew detektAll
./gradlew assembleDebug
./gradlew installDebug
```

Вручную на устройстве:
1. Вкладка «Карта» → тап по маркеру → в шторке тап по ветерану: карта плавно гаснет вместе с кнопками, ничего не зависает.
2. «Карта» → «Экскурсии» → выбрать экскурсию: то же самое.
3. «Карта» → переключить вкладку на «Ветераны» и обратно: камера сохраняется, карта не мигает.
4. Карточка ветерана с мини-картой → «Назад»: мини-карта гаснет вместе с экраном.
5. Спутник/схема, «Моё местоположение», масштаб жестами и тап по кластеру работают как раньше, плавность прокрутки не хуже.
6. Свернуть и развернуть приложение на экране карты: карта снова рисуется (не чёрная).
