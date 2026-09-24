package com.gorman.ourmemoryapp.ui.details.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.ui.common.ui.heroScrim

@Composable
fun DetailsHeader(veteran: Veteran, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(PORTRAIT_ASPECT_RATIO)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        AsyncImage(
            model = veteran.portrait,
            contentDescription = veteran.name,
            placeholder = painterResource(R.drawable.portrait_placeholder),
            error = painterResource(R.drawable.portrait_placeholder),
            fallback = painterResource(R.drawable.portrait_placeholder),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
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
                text = veteran.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (veteran.years.isNotBlank()) {
                Text(
                    text = veteran.years,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = YEARS_ALPHA),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private const val PORTRAIT_ASPECT_RATIO = 0.85f
private const val YEARS_ALPHA = 0.85f
