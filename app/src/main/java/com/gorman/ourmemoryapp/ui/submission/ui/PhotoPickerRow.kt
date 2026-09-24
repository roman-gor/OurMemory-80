package com.gorman.ourmemoryapp.ui.submission.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.CircleIconButton
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PhotoPickerRow(
    photoUris: ImmutableList<String>,
    canAddPhotos: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(photoUris, key = { _, uri -> uri }) { index, uri ->
            Box(modifier = Modifier.size(PHOTO_SIZE)) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(PHOTO_SIZE)
                        .clip(RoundedCornerShape(12.dp))
                )
                CircleIconButton(
                    painter = painterResource(R.drawable.close),
                    contentDescription = stringResource(R.string.remove_photo),
                    onClick = { onRemoveClick(index) },
                    containerColor = Color.Black.copy(alpha = REMOVE_BUTTON_ALPHA),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }
        if (canAddPhotos) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(PHOTO_SIZE)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddClick)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_photo),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.add_photos),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}

private val PHOTO_SIZE = 104.dp
private const val REMOVE_BUTTON_ALPHA = 0.5f
