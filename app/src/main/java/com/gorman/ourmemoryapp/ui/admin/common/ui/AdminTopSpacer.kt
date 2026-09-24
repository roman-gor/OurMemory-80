package com.gorman.ourmemoryapp.ui.admin.common.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminTopSpacer() {
    Spacer(modifier = Modifier.statusBarsPadding().height(TOP_BAR_HEIGHT))
}

private val TOP_BAR_HEIGHT = 56.dp
