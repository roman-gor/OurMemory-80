package com.gorman.ourmemoryapp.ui.admin.moderation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.moderation.models.ReviewPhotoUi
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ReviewPhotosRow(
    photos: ImmutableList<ReviewPhotoUi>,
    isEditable: Boolean,
    onToggle: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos, key = { it.url }) { photo ->
            Box(
                modifier = Modifier
                    .size(PHOTO_SIZE)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = isEditable) { onToggle(photo.url) }
            ) {
                AsyncImage(
                    model = photo.url,
                    contentDescription = stringResource(R.string.add_to_card),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(PHOTO_SIZE)
                        .alpha(if (photo.isSelected) 1f else DESELECTED_ALPHA)
                )
                Checkbox(
                    checked = photo.isSelected,
                    onCheckedChange = { onToggle(photo.url) },
                    enabled = isEditable,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

private val PHOTO_SIZE = 140.dp
private const val DESELECTED_ALPHA = 0.4f
