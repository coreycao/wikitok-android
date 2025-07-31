package com.sy.wikitok.data.repository

import com.sy.wikitok.data.model.Language
import com.sy.wikitok.data.db.LangDao
import com.sy.wikitok.data.model.AppUpdateInfo
import com.sy.wikitok.data.model.toEntity
import com.sy.wikitok.data.model.toModel
import com.sy.wikitok.network.AppUpdateApiService
import com.sy.wikitok.network.DownloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    private val langDao: LangDao
) {

    val scope = CoroutineScope(Dispatchers.IO)

    fun observerLanguages() = langDao.observeLanguages()
        .map { entities ->
            entities.map { entity ->
                entity.toModel()
            }
        }

    fun switchSelectedLanguage(oldLang: Language, newLang: Language) {
        scope.launch {
            langDao.switchSelectedLanguage(oldLang.toEntity(), newLang.toEntity())
        }
    }

    fun observeAppVersion(): Flow<Result<AppUpdateInfo>> = appUpdateApiService.observerVersionInfo()

    fun observeAppDownload(downloadUrl: String, downloadFile: File): Flow<DownloadState> =
        appUpdateApiService.downloadFile(downloadUrl, downloadFile)

}