package com.gorman.ourmemoryapp.ui.intro.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.CemeteryPhotos
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun IntroScreen(onStartClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(BACKGROUND_BLUR)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = CONTENT_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CONTENT_PADDING),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IntroTexts()
                }
            }
            StartButton(onClick = onStartClick)
        }
    }
}

@Composable
private fun IntroTexts() {
    Text(
        text = stringResource(R.string.app_name),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = mulishFont(),
            fontSize = TITLE_FONT_SIZE,
            color = colorResource(R.color.white),
            fontWeight = FontWeight.Bold,
            letterSpacing = TITLE_LETTER_SPACING
        )
    )
    Spacer(Modifier.height(TITLE_SPACING))
    Text(
        text = stringResource(R.string.slogan),
        style = TextStyle(
            fontFamily = mulishFont(),
            fontSize = BODY_FONT_SIZE,
            color = colorResource(R.color.white),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    )
    Spacer(Modifier.height(SECTION_SPACING))
    ImageSlideshow(CemeteryPhotos.all)
    Spacer(Modifier.height(SECTION_SPACING))
    Text(
        text = stringResource(R.string.idea),
        style = TextStyle(
            fontFamily = mulishFont(),
            fontSize = BODY_FONT_SIZE,
            color = colorResource(R.color.white),
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
private fun StartButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = BUTTON_VERTICAL_PADDING)
            .heightIn(min = BUTTON_MIN_HEIGHT)
            .widthIn(min = BUTTON_MIN_WIDTH),
        shape = RoundedCornerShape(CORNER_RADIUS),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.dark_red),
            contentColor = colorResource(R.color.white)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BUTTON_CONTENT_SPACING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.start),
                style = TextStyle(
                    fontFamily = mulishFont(),
                    fontSize = BUTTON_FONT_SIZE,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
            Icon(
                painter = painterResource(R.drawable.keyboard_arrow_right),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ImageSlideshow(imgList: List<Int>) {
    var currentIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(key1 = imgList) {
        while (true) {
            delay(SLIDE_INTERVAL_MILLIS.milliseconds)
            currentIndex = (currentIndex + 1) % imgList.size
        }
    }
    Crossfade(targetState = currentIndex) { index ->
        Image(
            painter = painterResource(imgList[index]),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(SLIDE_ASPECT_RATIO)
                .clip(RoundedCornerShape(CORNER_RADIUS))
        )
    }
}

private const val SCRIM_ALPHA = 0.5f
private const val SLIDE_ASPECT_RATIO = 2.5f
private const val SLIDE_INTERVAL_MILLIS = 4000L
private val BACKGROUND_BLUR = 8.dp
private val CONTENT_PADDING = 30.dp
private val TITLE_SPACING = 48.dp
private val SECTION_SPACING = 32.dp
private val BUTTON_VERTICAL_PADDING = 16.dp
private val BUTTON_MIN_HEIGHT = 64.dp
private val BUTTON_MIN_WIDTH = 220.dp
private val BUTTON_CONTENT_SPACING = 12.dp
private val CORNER_RADIUS = 12.dp
private val TITLE_FONT_SIZE = 38.sp
private val TITLE_LETTER_SPACING = 4.sp
private val BODY_FONT_SIZE = 20.sp
private val BUTTON_FONT_SIZE = 18.sp
