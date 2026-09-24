package com.gorman.ourmemoryapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.ui.main.viewmodels.MainViewModel
import com.gorman.ourmemoryapp.ui.navigation.ui.AppNavigation
import com.gorman.ourmemoryapp.ui.theme.OurMemoryAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openedFromLink = intent?.data != null
        setContent {
            val settings by mainViewModel.settings.collectAsStateWithLifecycle()
            val isDark = settings.themeMode.isDark(isSystemInDarkTheme())
            val density = LocalDensity.current

            LaunchedEffect(isDark) { applySystemBarStyle(isDark) }

            CompositionLocalProvider(
                LocalDensity provides Density(density.density, density.fontScale * settings.textScale.factor)
            ) {
                OurMemoryAppTheme(darkTheme = isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            openedFromLink = openedFromLink,
                            onChangeLangClick = ::updateLocale
                        )
                    }
                }
            }
        }
    }

    private fun applySystemBarStyle(isDark: Boolean) {
        val style = if (isDark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    private fun updateLocale(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}
