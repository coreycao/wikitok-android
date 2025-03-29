package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.Langs
import com.sy.wikitok.data.Language
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.WikiRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val _snakebarEvent = MutableSharedFlow<SnackbarEvent>()
    val snakebarEvent = _snakebarEvent.asSharedFlow()

    data class SnackbarEvent(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    )

    fun showSnackBar(message: String) {
        viewModelScope.launch {
            _snakebarEvent.emit(SnackbarEvent(message))
        }
    }

    val currentLangState = wikiRepository.currentLang().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Langs[wikiRepository.DEFAULT_LANG]
    )

    fun changeLanguage(lang: Language) {
        viewModelScope.launch {
            wikiRepository.changeLanguage(lang)
        }
    }

}