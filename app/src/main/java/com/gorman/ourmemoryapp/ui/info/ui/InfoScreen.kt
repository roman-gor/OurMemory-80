package com.gorman.ourmemoryapp.ui.info.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.CemeteryPhotos
import com.gorman.ourmemoryapp.ui.common.models.MediaUi
import com.gorman.ourmemoryapp.ui.common.ui.ExpandableTextSection
import com.gorman.ourmemoryapp.ui.common.ui.FloatingTopBar
import com.gorman.ourmemoryapp.ui.common.ui.MediaGallery
import com.gorman.ourmemoryapp.ui.common.ui.SystemBarIcons
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding
import com.gorman.ourmemoryapp.ui.common.ui.rememberIsHeroScrolledAway
import com.gorman.ourmemoryapp.ui.info.models.InfoContent
import com.gorman.ourmemoryapp.ui.more.models.AppLanguage
import kotlinx.collections.immutable.toPersistentList

@Composable
fun InfoScreen(
    onOpenMapClick: () -> Unit,
    onChangeLangClick: (String) -> Unit
) {
    val context = LocalContext.current
    val currentLanguage = AppLanguage.fromLanguage(LocalConfiguration.current.locales[0].language)
    val history = stringResource(R.string.information)
    val openingHours = stringResource(R.string.cemetery_opening_hours)
    val phone = stringResource(R.string.cemetery_phone)
    val paragraphs = remember(history) {
        history.split(PARAGRAPH_SEPARATOR).map { it.trim() }.filter { it.isNotBlank() }.toPersistentList()
    }
    val gallery = remember(context) {
        CemeteryPhotos.all.map { MediaUi(url = context.drawableUri(it), description = "") }.toPersistentList()
    }
    val listState = rememberLazyListState()
    val isCollapsed by rememberIsHeroScrolledAway(listState)

    SystemBarIcons(darkIcons = isCollapsed)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item { InfoHero() }
            item {
                ExpandableTextSection(
                    title = stringResource(R.string.historyInfoHeader),
                    paragraphs = paragraphs
                )
            }
            item { MediaGallery(title = stringResource(R.string.galleryHeader), media = gallery) }
            item { LocationSection(onOpenMapClick = onOpenMapClick) }
            if (openingHours.isNotBlank() || phone.isNotBlank()) {
                item { ContactsSection(openingHours = openingHours, phone = phone) }
            }
            item { NewsSection(news = InfoContent.news) }
            item { Spacer(modifier = Modifier.height(bottomBarContentPadding())) }
        }
        FloatingTopBar(
            title = stringResource(R.string.warHeader),
            isCollapsed = isCollapsed,
            onBackClick = null,
            modifier = Modifier.align(Alignment.TopCenter),
            actions = {
                LanguageMenuButton(currentLanguage = currentLanguage, onLanguageChange = onChangeLangClick)
            }
        )
    }
}

private fun Context.drawableUri(resId: Int) = "$ANDROID_RESOURCE_SCHEME$packageName/$resId"

private const val PARAGRAPH_SEPARATOR = "\n\n"
private const val ANDROID_RESOURCE_SCHEME = "android.resource://"
