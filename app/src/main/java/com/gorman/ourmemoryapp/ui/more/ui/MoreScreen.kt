package com.gorman.ourmemoryapp.ui.more.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.TextScale
import com.gorman.ourmemoryapp.domain.models.ThemeMode
import com.gorman.ourmemoryapp.ui.common.ui.LinkRow
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding
import com.gorman.ourmemoryapp.ui.common.ui.rememberQrScanAction
import com.gorman.ourmemoryapp.ui.more.models.AppLanguage
import com.gorman.ourmemoryapp.ui.more.models.MoreUiIntent
import com.gorman.ourmemoryapp.ui.more.models.labelRes
import com.gorman.ourmemoryapp.ui.more.viewmodels.MoreViewModel

@Composable
fun MoreScreen(
    onLanguageChange: (String) -> Unit,
    onWriteToUsClick: () -> Unit,
    onMyRequestsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onVeteranScanned: (String) -> Unit,
    onAdminClick: () -> Unit,
    moreViewModel: MoreViewModel = hiltViewModel()
) {
    val state by moreViewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val currentLanguage = AppLanguage.entries.firstOrNull {
        it.tag == LocalConfiguration.current.locales[0].language
    } ?: AppLanguage.RUSSIAN
    val onUiIntent = moreViewModel::onUiIntent
    val scanQr = rememberQrScanAction(onVeteranScanned = onVeteranScanned)

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + bottomBarContentPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        item { MoreTitle(text = stringResource(R.string.more)) }
        item {
            LinkRow(
                title = stringResource(R.string.my_requests),
                onClick = onMyRequestsClick,
                count = state.unseenRequestsCount
            )
        }
        item { LinkRow(title = stringResource(R.string.favorites), onClick = onFavoritesClick) }
        item { LinkRow(title = stringResource(R.string.scan_qr_code), onClick = scanQr) }
        item { LinkRow(title = stringResource(R.string.write_to_us), onClick = onWriteToUsClick) }
        item { MoreSectionTitle(text = stringResource(R.string.appearance)) }
        item {
            ChoiceSetting(
                title = stringResource(R.string.theme),
                options = ThemeMode.entries,
                selected = settings.themeMode,
                labelRes = { it.labelRes },
                onSelect = { onUiIntent(MoreUiIntent.OnThemeModeChange(it)) }
            )
        }
        item {
            ChoiceSetting(
                title = stringResource(R.string.text_size),
                options = TextScale.entries,
                selected = settings.textScale,
                labelRes = { it.labelRes },
                onSelect = { onUiIntent(MoreUiIntent.OnTextScaleChange(it)) }
            )
        }
        item {
            ChoiceSetting(
                title = stringResource(R.string.language),
                options = AppLanguage.entries,
                selected = currentLanguage,
                labelRes = { it.labelRes },
                onSelect = { onLanguageChange(it.tag) }
            )
        }
        item { MoreSectionTitle(text = stringResource(R.string.reminders)) }
        item {
            SwitchSetting(
                title = stringResource(R.string.victory_day),
                isChecked = settings.victoryDayReminder,
                onCheckedChange = { onUiIntent(MoreUiIntent.OnVictoryDayReminderChange(it)) }
            )
        }
        item {
            SwitchSetting(
                title = stringResource(R.string.memorable_days_of_favorites),
                isChecked = settings.favoriteReminders,
                onCheckedChange = { onUiIntent(MoreUiIntent.OnFavoriteRemindersChange(it)) }
            )
        }
        item {
            TextButton(onClick = onAdminClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.sign_in_as_admin))
            }
        }
    }
}

@Composable
private fun MoreTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun MoreSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
