package com.gorman.ourmemoryapp.ui.info.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.heroScrim

@Composable
fun InfoHero(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(HERO_ASPECT_RATIO)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Image(
            painter = painterResource(R.drawable.warwar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(heroScrim)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.warHeader),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.address),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = ADDRESS_ALPHA),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private const val HERO_ASPECT_RATIO = 1.1f
private const val ADDRESS_ALPHA = 0.85f
