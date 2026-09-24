package com.gorman.ourmemoryapp.ui.info.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.SectionTitle
import com.gorman.ourmemoryapp.ui.info.models.NewsUi
import kotlinx.collections.immutable.ImmutableList

@Composable
fun NewsSection(news: ImmutableList<NewsUi>, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(R.string.news))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            news.forEach { item ->
                NewsRow(
                    news = item,
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, item.url.toUri()))
                        } catch (_: ActivityNotFoundException) {
                            Unit
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NewsRow(news: NewsUi, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Image(
            painter = painterResource(news.iconRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Text(
            text = news.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
