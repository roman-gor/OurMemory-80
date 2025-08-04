package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.Image
import com.gorman.ourmemoryapp.data.imagesList
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.util.Locale

@Composable
fun InfoScreen(navigateToBack: () -> Unit, navigateToImage: () -> Unit, onChangeLangClick: (String) -> Unit)
{
    val lifecycleOwner = LocalLifecycleOwner.current
    val isVisible = remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                isVisible.value = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.White)
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(navigateToBack = {navigateToBack()}, onChangeLangClick = onChangeLangClick)
        TextHead(R.string.historyInfoHeader)
        InfoText()
        TextHead(R.string.galleryHeader)
        //TODO onClick event
        Images(navigateToImage)
        TextHead(R.string.locationHeader)
        Text(
            text = stringResource(R.string.address),
            style = TextStyle(
                fontFamily = mulishFont(),
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colorResource(R.color.dark_red)
            ),
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .fillMaxWidth()
        )
        if (isVisible.value)
            YandexMapView()
    }
}

@Composable
fun InfoText(){
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ){
        Text(text = stringResource(R.string.information),
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = mulishFont(),
            style = TextStyle(fontWeight = FontWeight.Normal),
            modifier = Modifier
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                .verticalScroll(rememberScrollState()),
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun TextHead(text: Int){
    Text(
        text = stringResource(text),
        style = TextStyle(
            fontFamily = mulishFont(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            letterSpacing = 0.3.sp,
            color = colorResource(R.color.dark_red)
        ),
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            .fillMaxWidth()
    )
}

@Composable
fun Header(navigateToBack: () -> Unit, onChangeLangClick: (String) -> Unit){
    val locale: Locale = LocalConfiguration.current.locales[0]
    Row (
        modifier = Modifier.fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Button(onClick = {
            navigateToBack()
        },
            modifier = Modifier.padding(start = 8.dp)
                .aspectRatio(1f)
                .weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0F0F0),
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorResource(R.color.dark_red)
            )
        }
        Text(stringResource(R.string.warHeader),
            modifier = Modifier.weight(5f),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = mulishFont(),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_red),
                fontSize = 18.sp
            ))
        Button(onClick = {
            val newLang = if (locale.language == "ru") "be" else "ru"
            onChangeLangClick(newLang)
        },
            modifier = Modifier.padding(start = 8.dp)
                .aspectRatio(1f)
                .weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0F0F0),
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp)) {
            Text(text = if (locale.language == "ru") "БЕЛ" else "РУС",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.dark_red),
                    fontSize = 12.sp,
                    fontFamily = mulishFont()
                )
            )
        }
    }
}

@Composable
fun YandexMapView(){
    val context = androidx.compose.ui.platform.LocalContext.current
    val mapView = remember { MapView(context) }
    DisposableEffect(Unit) {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()

        onDispose {
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }
    val locationPoint = Point(53.908775, 27.586246)
    mapView.mapWindow.map.move(CameraPosition(
        locationPoint,
        15.0f,
        0.0f,
        0.0f
    ))
    val imageProvider = ImageProvider.fromResource(context, R.drawable.ic_marker)
    mapView.mapWindow.map.mapObjects.addPlacemark().apply{
        geometry = locationPoint
        setIcon(imageProvider)
    }
    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun Images(onClick: () -> Unit){
    LazyHorizontalGrid(
        rows = GridCells.Fixed(1),
        modifier = Modifier
            .height(220.dp)
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
        items(imagesList){
            img->
            ImageItem(image = img, onClick = {onClick})
        }
    }
}

@Composable
fun ImageItem(image: Image, onClick: () -> Unit){
    val imagePainter = painterResource(image.res)
    val aspectRatio = with(LocalDensity.current) {
        val size = imagePainter.intrinsicSize
        if (size.height > 0) size.width / size.height else 1f
    }
    Card(
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "Images of place",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(220.dp)
                .aspectRatio(aspectRatio.coerceIn(0.5f, 2f))
                .clip(RoundedCornerShape(12.dp))
        )
    }
}