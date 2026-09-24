package com.gorman.ourmemoryapp.ui.admin.common.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R

@Composable
fun AdminAddButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(painter = painterResource(R.drawable.add), contentDescription = null) },
        text = { Text(text = text) },
        modifier = modifier
            .navigationBarsPadding()
            .padding(BUTTON_PADDING)
    )
}

private val BUTTON_PADDING = 16.dp
