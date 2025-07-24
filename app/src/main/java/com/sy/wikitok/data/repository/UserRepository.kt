package com.sy.wikitok.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sy.wikitok.data.DEFAULT_LANGUAGE
import com.sy.wikitok.data.DEFAULT_LANG_ID
import com.sy.wikitok.data.Langs
import com.sy.wikitok.data.Language
import com.sy.wikitok.data.model.AppUpdateInfo
import com.sy.wikitok.network.AppUpdateApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Yeung
 * @date 2025/3/31
 *
 * Repo for user info and settings.
 *
 */
class UserRepository(
    private val appUpdateApiService: AppUpdateApiService,
    private val dataStore: DataStore<Preferences>
) {

    suspend fun updateLanguage(lang: Language) {
        dataStore.edit { preference ->
            preference[KEY_LANG] = lang.id
        }
    }

    fun observeAppVersion(): Flow<Result<AppUpdateInfo>> = appUpdateApiService.observerVersionInfo()

}