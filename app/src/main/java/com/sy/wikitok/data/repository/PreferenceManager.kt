package com.sy.wikitok.data.repository

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * @author Yeung
 * @date 2025/4/11
 */

private const val DATASTORE_SETTINGS = "settings"

private const val DATASTORE_SUMMARY = "summary"

val Application.summaryDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_SUMMARY)

private const val DATASTORE_CONFIG = "config"

val Application.configDataStore : DataStore<Preferences> by preferencesDataStore(name = DATASTORE_CONFIG)