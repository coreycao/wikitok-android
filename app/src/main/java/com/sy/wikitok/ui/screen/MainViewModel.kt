package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.Language
import com.sy.wikitok.data.model.AppUpdateInfo
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.UserRepository
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.utils.Logger
import com.sy.wikitok.utils.currentDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * @author Yeung
 * @date 2025/3/20
 */
class MainViewModel(private val userRepo: UserRepository, private val wikiRepo: WikiRepository) :
    ViewModel() {

    sealed class SettingDialogState {
        object None : SettingDialogState()
        data class AppUpdateDialog(
            val checkedSuccess: Boolean = true,
            val versionInfo: AppUpdateInfo? = null
        ) :
            SettingDialogState()

        object AboutMessageDialog : SettingDialogState()
        object LanguageOption : SettingDialogState()
        data class ExportFavorite(val result: Result<String>) : SettingDialogState()
    }

    private val _settingDialogState = MutableStateFlow<SettingDialogState>(SettingDialogState.None)
    val settingDialogState = _settingDialogState.asStateFlow()

    fun showAboutMessageDialog() {
        viewModelScope.launch {
            _settingDialogState.value = SettingDialogState.AboutMessageDialog
        }
    }

    fun showLanguageOptionDialog() {
        viewModelScope.launch {
            _settingDialogState.value = SettingDialogState.LanguageOption
        }
    }

    private fun showAppUpdateDialog(versionInfo: AppUpdateInfo) {
        viewModelScope.launch {
            _settingDialogState.value = SettingDialogState.AppUpdateDialog(true, versionInfo)
        }
    }

    fun dismissDialog() {
        viewModelScope.launch {
            _settingDialogState.value = SettingDialogState.None
        }
    }

    fun changeLanguage(lang: Language) {
        viewModelScope.launch {
            userRepo.updateLanguage(lang)
        }
    }

    fun checkAppUpdate() {
        viewModelScope.launch {
            userRepo.observeAppVersion()
                .onEach {
                    if (it.isSuccess) {
                        val appUpdateInfo: AppUpdateInfo = it.getOrThrow()
                        showAppUpdateDialog(appUpdateInfo)
                    } else {
                        _settingDialogState.value = SettingDialogState.AppUpdateDialog(false)
                    }
                }.catch {
                    _settingDialogState.value = SettingDialogState.AppUpdateDialog(false)
                }.collect { value ->
                    Logger.d(tag = "checkAppUpdate", message = "value: $value")
                }
        }
    }

    @Serializable
    data class ExportData(
        val exportTime: String,
        val favorites: List<WikiModel>
    )

    fun exportFavorite() {
        viewModelScope.launch {
            _settingDialogState.value = SettingDialogState.ExportFavorite(
                runCatching {
                    Json.encodeToString(
                        ExportData(currentDateTime(), wikiRepo.readLocalFavorites())
                    )
                }
            )
        }
    }
}