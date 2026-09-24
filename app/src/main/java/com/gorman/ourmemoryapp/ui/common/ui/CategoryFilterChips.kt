package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R

@Composable
fun CategoryFilterChips(
    checkedWar: Boolean,
    checkedArt: Boolean,
    onCheckedWarChange: (Boolean) -> Unit,
    onCheckedArtChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    trailingContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryFilterChip(
            selected = checkedWar,
            label = stringResource(R.string.heroUSSR),
            onClick = { onCheckedWarChange(!checkedWar) }
        )
        CategoryFilterChip(
            selected = checkedArt,
            label = stringResource(R.string.art),
            onClick = { onCheckedArtChange(!checkedArt) }
        )
        trailingContent()
    }
}

@Composable
private fun CategoryFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
    )
}
