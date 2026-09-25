package com.gorman.ourmemoryapp.data.settings.language

import androidx.appcompat.app.AppCompatDelegate
import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import java.util.Locale

class AppLocaleContentLanguageProvider : ContentLanguageProvider {
    override fun current(): ContentLanguage? {
        val locale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
        return ContentLanguage.fromLanguage(locale.language)
    }
}
