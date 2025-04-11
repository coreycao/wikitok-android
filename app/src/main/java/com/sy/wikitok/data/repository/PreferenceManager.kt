package com.sy.wikitok.data.repository

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * @author Yeung
 * @date 2025/4/11
 */

private const val SETTINGS_NAME = "settings"

val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_NAME)