package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gorman.ourmemoryapp.R

fun offensiveWordsHint(isShown: Boolean): (@Composable () -> Unit)? =
    if (isShown) {
        { Text(text = stringResource(R.string.remove_offensive_words_msg)) }
    } else {
        null
    }
