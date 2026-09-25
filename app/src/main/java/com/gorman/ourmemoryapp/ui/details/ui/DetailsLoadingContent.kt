package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.ui.common.ui.placeholder
import com.gorman.ourmemoryapp.ui.common.ui.rememberPlaceholderAlpha

@Composable
fun DetailsLoadingContent(modifier: Modifier = Modifier) {
    val alphaState = rememberPlaceholderAlpha()
    val alpha = { alphaState.value }
    val color = MaterialTheme.colorScheme.surfaceContainerHighest
    Column(
        verticalArrangement = Arrangement.spacedBy(28.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(HERO_ASPECT_RATIO)
                .placeholder(color = color, alpha = alpha, shape = RectangleShape)
        )
        Spacer(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(84.dp)
                .placeholder(color = color, alpha = alpha, shape = RoundedCornerShape(20.dp))
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            listOf(SECTION_TITLE_FRACTION, 1f, 1f, 1f, LAST_LINE_FRACTION).forEach { fraction ->
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(16.dp)
                        .placeholder(color = color, alpha = alpha)
                )
            }
        }
    }
}

private const val HERO_ASPECT_RATIO = 0.85f
private const val SECTION_TITLE_FRACTION = 0.4f
private const val LAST_LINE_FRACTION = 0.7f
