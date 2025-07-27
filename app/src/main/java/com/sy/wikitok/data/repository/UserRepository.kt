package com.sy.wikitok.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sy.wikitok.data.Language
import com.sy.wikitok.data.model.AppUpdateInfo
import com.sy.wikitok.network.AppUpdateApiService
import com.sy.wikitok.network.DownloadState
import kotlinx.coroutines.flow.Flow
import java.io.File

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

    fun observeAppDownload(downloadUrl:String,downloadFile: File): Flow<DownloadState> =
        appUpdateApiService.downloadFile(downloadUrl, downloadFile)

}