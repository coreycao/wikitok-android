package com.sy.wikitok.data.repository

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * @author Yeung
 * @date 2025/4/11
 */

private const val SETTINGS_NAME = "settings"

val Application.settingDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_NAME)

val KEY_LANG = stringPreferencesKey("lang")

private const val SUMMARY_NAME = "summary"

val Application.summaryDataStore: DataStore<Preferences> by preferencesDataStore(name = SUMMARY_NAME)