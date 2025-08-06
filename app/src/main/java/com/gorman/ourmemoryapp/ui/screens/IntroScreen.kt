package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.Image
import com.gorman.ourmemoryapp.data.imagesList
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(8.dp)
        )
        Content { onStartClick() }
    }
}

@Composable
fun Content(onStartClick: () -> Unit){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.app_name),
            style = TextStyle(
                fontFamily = mulishFont(),
                fontSize = 38.sp,
                color = colorResource(R.color.white),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ))
        Spacer(Modifier.height(60.dp))
        Text(text = stringResource(R.string.slogan),
            style = TextStyle(
                fontFamily = mulishFont(),
                fontSize = 20.sp,
                color = colorResource(R.color.white),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ))
        Spacer(Modifier.height(36.dp))
        ImageSlideshow(imagesList)
        Spacer(Modifier.height(36.dp))
        Text(text = stringResource(R.string.idea),
            style = TextStyle(
                fontFamily = mulishFont(),
                fontSize = 20.sp,
                color = colorResource(R.color.white),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            ))
        Spacer(Modifier.height(60.dp))
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .height(70.dp)
                .width(220.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.dark_red),
                contentColor = colorResource(R.color.white)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Начать",
                    modifier = Modifier.padding(start = 8.dp),
                    style = TextStyle(
                        fontFamily = mulishFont(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}

@Composable
fun ImageSlideshow(imgList: List<Image>){
    var currentIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(key1 = imgList) {
        while(true){
            delay(4000L)
            currentIndex = (currentIndex + 1) % imgList.size
        }
    }
    Crossfade(targetState = currentIndex) {index->
        val imagePainter = painterResource(imgList[index].res)
        Image(
            painter = imagePainter,
            contentDescription = "Images of place",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(150.dp)
                .aspectRatio(2.5f)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}
