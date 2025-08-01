package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.Language
import com.sy.wikitok.data.model.defaultLanguage
import com.sy.wikitok.data.model.AppUpdateInfo
import com.sy.wikitok.data.repository.UserRepository
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.network.DownloadState
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * @author Yeung
 * @date 2025/3/20
 */
class SettingViewModel(
    private val userRepo: UserRepository,
    private val wikiRepo: WikiRepository
) : ViewModel() {

    init {
        Logger.d(tag = "SettingVM", message = "onCreated")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(tag = "SettingVM", message = "onCleared")
    }

    sealed class Effect {
        data class Toast(val message: String) : Effect()
        data class Export(val exportedData: String) : Effect()
        data class Update(val versionInfo: AppUpdateInfo) : Effect()
    }

    sealed class DialogState {
        object None : DialogState()
        object About : DialogState()
        object Option : DialogState()
    }

    sealed class DownloadUiState {
        object None : DownloadUiState()
        data class Downloading(val progress: Float) : DownloadUiState()
        data class Completed(val file: File) : DownloadUiState()
    }

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    private val _dialogState = MutableStateFlow<DialogState>(DialogState.None)
    val dialogState = _dialogState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadUiState>(DownloadUiState.None)
    val downloadState = _downloadState.asStateFlow()

    val languages = userRepo.observerLanguages()
        .onEach {
            Logger.d("Available languages: ${it.joinToString { lang -> lang.name }}")
        }
        .filter { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(defaultLanguage())
        )

    fun showAboutDialog() {
        viewModelScope.launch {
            _dialogState.value = DialogState.About
        }
    }

    fun showLangOptDialog() {
        viewModelScope.launch {
            _dialogState.value = DialogState.Option
        }
    }

    fun dismissDialog() {
        viewModelScope.launch {
            _dialogState.value = DialogState.None
        }
    }

    fun changeLanguage(oldLang: Language, newLang: Language) {
        viewModelScope.launch {
            Logger.d("changeLanguage from ${oldLang.name} to ${newLang.name}")
            userRepo.switchSelectedLanguage(oldLang, newLang)
        }
    }

    private var cheekAppUpdateJob: Job? = null

    fun checkAppUpdate() {
        if (cheekAppUpdateJob?.isActive == true) return
        cheekAppUpdateJob = viewModelScope.launch {
            userRepo.observeAppVersion()
                .onEach { result ->
                    result.fold(
                        onSuccess = { it ->
                            _effect.emit(Effect.Update(it))
                        },
                        onFailure = {
                            _effect.emit(Effect.Toast("检查更新失败"))
                        }
                    )
                }.first()
        }
    }

    fun downloadAppUpdate(versionInfo: AppUpdateInfo, downloadDir: File?) {
        viewModelScope.launch {
            if (downloadDir == null) {
                Logger.e("downloadDir == null")
                _effect.emit(Effect.Toast("下载失败"))
                return@launch
            }
            if (downloadDir.exists().not()) {
                downloadDir.mkdirs()
            }
            val apkUrl = versionInfo.downloadUrl.android.url
            val apkName = "${versionInfo.downloadUrl.android.md5}.apk"
            val apkFile = File(downloadDir, apkName)
            if (apkFile.exists()) {
                Logger.d("already downloaded")
                _effect.emit(Effect.Toast("下载完成"))
                _downloadState.value = DownloadUiState.Completed(apkFile)
                return@launch
            }
            _effect.emit(Effect.Toast("开始下载"))
            userRepo.observeAppDownload(apkUrl, apkFile)
                .collect { state ->
                    when (state) {
                        is DownloadState.Error -> {
                            _effect.emit(Effect.Toast("下载失败"))
                        }

                        is DownloadState.Progress -> {
                            _downloadState.value = DownloadUiState.Downloading(state.progress)
                        }

                        is DownloadState.Success -> {
                            _effect.emit(Effect.Toast("下载完成"))
                            _downloadState.value = DownloadUiState.Completed(apkFile)
                        }
                    }
                }
        }
    }

    fun exportFavorites() {
        viewModelScope.launch {
            val favorites = wikiRepo.readLocalFavorites()
            if (favorites.isEmpty()) {
                _effect.emit(Effect.Toast("您的收藏为空"))
            } else {
                val exportedData = withContext(Dispatchers.IO) {
                    Json.encodeToString(favorites)
                }
                _effect.emit(Effect.Export(exportedData))
            }
        }
    }
}