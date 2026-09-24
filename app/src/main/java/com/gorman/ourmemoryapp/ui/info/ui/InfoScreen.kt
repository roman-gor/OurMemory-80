package com.gorman.ourmemoryapp.ui.info.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.gorman.ourmemoryapp.ui.common.ui.PillButton
import com.gorman.ourmemoryapp.ui.common.ui.SystemBarIcons
import com.gorman.ourmemoryapp.ui.common.ui.rememberIsHeroScrolledAway
import com.gorman.ourmemoryapp.ui.info.models.InfoContent
import kotlinx.collections.immutable.toPersistentList

@Composable
fun InfoScreen(
    onOpenMapClick: () -> Unit,
    onChangeLangClick: (String) -> Unit,
    onWriteToUsClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val context = LocalContext.current
    val isRussian = LocalConfiguration.current.locales[0].language == RUSSIAN_LANGUAGE
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
            item {
                OutlinedButton(
                    onClick = onWriteToUsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = stringResource(R.string.write_to_us))
                }
            }
            item {
                TextButton(onClick = onAdminClick, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.sign_in_as_admin))
                }
            }
            item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
        FloatingTopBar(
            title = stringResource(R.string.warHeader),
            isCollapsed = isCollapsed,
            onBackClick = null,
            modifier = Modifier.align(Alignment.TopCenter),
            actions = {
                PillButton(
                    text = stringResource(if (isRussian) R.string.bel else R.string.rus),
                    onClick = { onChangeLangClick(if (isRussian) BELARUSIAN_LANGUAGE else RUSSIAN_LANGUAGE) }
                )
            }
        )
    }
}

private fun Context.drawableUri(resId: Int) = "$ANDROID_RESOURCE_SCHEME$packageName/$resId"

private const val RUSSIAN_LANGUAGE = "ru"
private const val BELARUSIAN_LANGUAGE = "be"
private const val PARAGRAPH_SEPARATOR = "\n\n"
private const val ANDROID_RESOURCE_SCHEME = "android.resource://"
