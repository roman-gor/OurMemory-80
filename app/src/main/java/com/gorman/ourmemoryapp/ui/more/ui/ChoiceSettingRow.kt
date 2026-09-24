package com.gorman.ourmemoryapp.ui.more.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.SettingRow
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> ChoiceSettingRow(
    @DrawableRes iconRes: Int,
    title: String,
    options: ImmutableList<T>,
    selected: T,
    labelRes: (T) -> Int,
    onSelect: (T) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Box {
        SettingRow(
            iconRes = iconRes,
            title = title,
            onClick = { isExpanded = true },
            trailing = { SelectedValue(text = stringResource(labelRes(selected)), isExpanded = isExpanded) }
        )
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                shape = RoundedCornerShape(MENU_CORNER_RADIUS),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                offset = DpOffset(x = MENU_OFFSET_X, y = 0.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(labelRes(option)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        onClick = {
                            isExpanded = false
                            onSelect(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedValue(text: String, isExpanded: Boolean) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) ARROW_EXPANDED_ROTATION else 0f,
        label = "arrowRotation"
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(VALUE_SPACING)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.rotate(arrowRotation)
        )
    }
}

private val MENU_CORNER_RADIUS = 20.dp
private val MENU_OFFSET_X = (-12).dp
private val VALUE_SPACING = 2.dp
private const val ARROW_EXPANDED_ROTATION = 180f
