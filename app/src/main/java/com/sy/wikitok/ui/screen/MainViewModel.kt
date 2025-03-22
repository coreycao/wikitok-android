package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.WikiRepository

/**
 * @author Yeung
 * @date 2025/3/20
 */
class MainViewModel(private val wikiRepository: WikiRepository) : ViewModel() {

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val wikiList: List<WikiModel>) : UiState()
        data class Error(val message: String) : UiState()
    }

}