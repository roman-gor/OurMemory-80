package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.fonts.inriaFont
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

@Composable
fun InfoScreen(navigateToBack: () -> Unit)
{
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.White)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
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
            Text("Военное кладбище",
                modifier = Modifier.weight(5f),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = mulishFont(),
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_red),
                    fontSize = 18.sp
                ))
            Button(onClick = {},
                modifier = Modifier.padding(start = 8.dp)
                    .aspectRatio(1f)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)) {
                Text("",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 26.sp,
                        fontFamily = inriaFont()
                    ),
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.dark_red)
                )
            }
        }
        YandexMapView()
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
    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
            .height(200.dp)
    )
}
