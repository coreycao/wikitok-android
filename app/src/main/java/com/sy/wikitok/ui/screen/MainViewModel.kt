package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.repository.ConfigRepository
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/7/27
 */
class MainViewModel(private val configRepository: ConfigRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            configRepository.fetchRemoteConfig()
        }
    }
}