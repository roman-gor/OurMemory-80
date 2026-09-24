package com.gorman.ourmemoryapp.ui.admin.common.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R

@Composable
fun GuideHintCard(title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(HINT_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(HINT_PADDING)) {
            Icon(painter = painterResource(R.drawable.menu_book), contentDescription = null)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = HINT_TEXT_PADDING)
            )
            Icon(painter = painterResource(R.drawable.keyboard_arrow_right), contentDescription = null)
        }
    }
}

private val HINT_CORNER_RADIUS = 16.dp
private val HINT_PADDING = 14.dp
private val HINT_TEXT_PADDING = 12.dp
