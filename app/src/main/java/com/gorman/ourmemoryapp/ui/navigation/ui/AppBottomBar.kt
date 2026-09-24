package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.ui.navigation.models.TopLevelTab
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AppBottomBar(
    tabs: ImmutableList<TopLevelTab>,
    selectedTab: TopLevelTab?,
    onTabClick: (TopLevelTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = BAR_ALPHA),
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = BAR_HORIZONTAL_PADDING, vertical = BAR_VERTICAL_PADDING)
            .fillMaxWidth()
            .height(BAR_HEIGHT)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = BAR_INNER_PADDING)
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                FloatingTabItem(
                    tab = tab,
                    isSelected = isSelected,
                    onClick = { onTabClick(tab) },
                    modifier = if (isSelected) Modifier.weight(1f, fill = false) else Modifier
                )
            }
        }
    }
}

@Composable
private fun FloatingTabItem(
    tab: TopLevelTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(tab.labelRes)
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "tabContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabContent"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(containerColor)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = ITEM_HORIZONTAL_PADDING, vertical = ITEM_VERTICAL_PADDING)
            .animateContentSize()
    ) {
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(ICON_SIZE)
        )
        AnimatedVisibility(
            visible = isSelected,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = LABEL_START_PADDING)
            )
        }
    }
}

val BAR_HEIGHT = 64.dp
val BAR_VERTICAL_PADDING = 8.dp
private val BAR_HORIZONTAL_PADDING = 16.dp
private val BAR_INNER_PADDING = 6.dp
private val ITEM_HORIZONTAL_PADDING = 10.dp
private val ITEM_VERTICAL_PADDING = 6.dp
private val LABEL_START_PADDING = 6.dp
private val ICON_SIZE = 22.dp
private const val BAR_ALPHA = 0.96f
