package com.gorman.ourmemoryapp.ui.admin.guide.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.guide.models.GuideSection

@Composable
fun GuideSectionCard(
    section: GuideSection,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) ARROW_EXPANDED_ROTATION else 0f,
        label = "guideArrow"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CARD_PADDING)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CONTENT_SPACING)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(ICON_CONTAINER_SIZE)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        painter = painterResource(section.iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(ICON_SIZE)
                    )
                }
                Text(
                    text = stringResource(section.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.keyboard_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GuideSectionBody(section = section)
            }
        }
    }
}

@Composable
private fun GuideSectionBody(section: GuideSection) {
    Column(
        verticalArrangement = Arrangement.spacedBy(CONTENT_SPACING),
        modifier = Modifier.padding(top = BODY_TOP_PADDING)
    ) {
        stringArrayResource(section.stepsRes).forEachIndexed { index, step ->
            GuideStep(number = index + 1, text = step)
        }
        Text(
            text = stringResource(section.tipRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TIP_CORNER_RADIUS))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(TIP_PADDING)
        )
    }
}

@Composable
private fun GuideStep(number: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(CONTENT_SPACING)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(STEP_NUMBER_SIZE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

private val CARD_CORNER_RADIUS = 20.dp
private val CARD_PADDING = 16.dp
private val CONTENT_SPACING = 12.dp
private val ICON_CONTAINER_SIZE = 40.dp
private val ICON_SIZE = 20.dp
private val BODY_TOP_PADDING = 16.dp
private val STEP_NUMBER_SIZE = 24.dp
private val TIP_CORNER_RADIUS = 12.dp
private val TIP_PADDING = 12.dp
private const val ARROW_EXPANDED_ROTATION = 180f
