package com.gorman.ourmemoryapp.ui.more.ui

import androidx.annotation.DrawableRes
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import com.gorman.ourmemoryapp.ui.common.ui.SettingRow

@Composable
fun SwitchSettingRow(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingRow(
        iconRes = iconRes,
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!isChecked) },
        trailing = { Switch(checked = isChecked, onCheckedChange = onCheckedChange) }
    )
}
