package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.veterans.models.InfoBlock

@Composable
fun InfoBlockEditor(
    block: InfoBlock,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onChange: (InfoBlock) -> Unit,
    onMove: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            when (block) {
                is InfoBlock.Paragraph -> OutlinedTextField(
                    value = block.text,
                    onValueChange = { onChange(block.copy(text = it)) },
                    label = { Text(text = stringResource(R.string.paragraph)) },
                    minLines = PARAGRAPH_MIN_LINES,
                    modifier = Modifier.fillMaxWidth()
                )
                is InfoBlock.Media -> {
                    AsyncImage(
                        model = block.url,
                        contentDescription = block.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MEDIA_HEIGHT)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    OutlinedTextField(
                        value = block.caption,
                        onValueChange = { onChange(block.copy(caption = it)) },
                        label = { Text(text = stringResource(R.string.caption)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Row {
                IconButton(onClick = { onMove(-1) }, enabled = canMoveUp) {
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_up),
                        contentDescription = stringResource(R.string.move_up),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onMove(1) }, enabled = canMoveDown) {
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_down),
                        contentDescription = stringResource(R.string.move_down),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private val MEDIA_HEIGHT = 160.dp
private const val PARAGRAPH_MIN_LINES = 3
