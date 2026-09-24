package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BiographySection(paragraphs: ImmutableList<String>, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var firstParagraphOverflows by remember { mutableStateOf(false) }
    val visibleParagraphs = if (expanded) paragraphs else paragraphs.take(1)

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(R.string.biography))
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            visibleParagraphs.forEachIndexed { index, paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layout ->
                        if (index == 0 && !expanded) firstParagraphOverflows = layout.hasVisualOverflow
                    }
                )
            }
        }
        if (paragraphs.size > 1 || firstParagraphOverflows || expanded) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = stringResource(if (expanded) R.string.rollup else R.string.expand))
            }
        }
    }
}

private const val COLLAPSED_MAX_LINES = 8
