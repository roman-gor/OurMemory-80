package com.gorman.ourmemoryapp.ui.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.ui.common.ui.placeholder
import com.gorman.ourmemoryapp.ui.common.ui.rememberPlaceholderAlpha

@Composable
fun HomeLoadingContent(modifier: Modifier = Modifier) {
    val alphaState = rememberPlaceholderAlpha()
    val alpha = { alphaState.value }
    val color = MaterialTheme.colorScheme.surfaceContainerHighest
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .size(width = TITLE_WIDTH, height = 28.dp)
                    .placeholder(color = color, alpha = alpha)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .placeholder(color = color, alpha = alpha, shape = RoundedCornerShape(14.dp))
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(CHIPS_COUNT) {
                    Spacer(
                        modifier = Modifier
                            .size(width = CHIP_WIDTH, height = 32.dp)
                            .placeholder(color = color, alpha = alpha, shape = CircleShape)
                    )
                }
            }
        }
        repeat(ROWS_COUNT) {
            VeteranItemPlaceholder(color = color, alpha = alpha)
        }
    }
}

@Composable
private fun VeteranItemPlaceholder(color: Color, alpha: () -> Float) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(10.dp)
    ) {
        Spacer(
            modifier = Modifier
                .weight(PORTRAIT_WEIGHT)
                .aspectRatio(PORTRAIT_ASPECT_RATIO)
                .placeholder(color = color, alpha = alpha, shape = RoundedCornerShape(10.dp))
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(TEXT_WEIGHT)
        ) {
            listOf(NAME_FRACTION, YEARS_FRACTION, 1f, 1f, LAST_LINE_FRACTION).forEach { fraction ->
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(14.dp)
                        .placeholder(color = color, alpha = alpha)
                )
            }
        }
    }
}

private val TITLE_WIDTH = 180.dp
private val CHIP_WIDTH = 96.dp
private const val CHIPS_COUNT = 2
private const val ROWS_COUNT = 6
private const val PORTRAIT_WEIGHT = 1f
private const val TEXT_WEIGHT = 2f
private const val PORTRAIT_ASPECT_RATIO = 0.8f
private const val NAME_FRACTION = 0.7f
private const val YEARS_FRACTION = 0.4f
private const val LAST_LINE_FRACTION = 0.6f
