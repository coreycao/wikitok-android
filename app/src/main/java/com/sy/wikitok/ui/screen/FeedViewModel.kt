package com.sy.wikitok.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.GenAIRepository
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FeedViewModel(
    private val wikiRepository: WikiRepository,
    private val genAIRepository: GenAIRepository
) : ViewModel() {

    sealed class Effect {
        data class Toast(val message: String) : Effect()
    }

    sealed class UiState {
        data class Success(val wikiList: List<WikiModel>) : UiState()
        data class Error(val message: String) : UiState()
        object Loading : UiState()
        object Empty : UiState()
    }

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    // keep view pager state
    var currentPage by mutableIntStateOf(0)
        private set

    fun updateCurrentPage(page: Int) {
        currentPage = page
    }

    var isRefreshing by mutableStateOf(false)
        private set

    var refreshJob: Job? = null

    fun refresh() {
        if (refreshJob?.isActive == true) {
            Logger.d(tag = "FeedViewModel", message = "refreshJob is already running")
            return
        }
        isRefreshing = true
        refreshJob = viewModelScope.launch {
            wikiRepository.observableRemoteWikiFeed.first().fold(
                onSuccess = {
                    Logger.d("refresh success")
                    updateCurrentPage(0)
                }, onFailure = {
                    Logger.d("refresh failed: $it")
                    _effect.emit(Effect.Toast("refresh failed, try again"))
                }
            )
            isRefreshing = false
        }
    }

    val feedUiState = wikiRepository.observableFeed
        .map { list ->
            if (list.isEmpty()) {
                Logger.d(tag = "FeedViewModel", message = "feedDB: empty")
                UiState.Empty
            } else {
                Logger.d(tag = "FeedViewModel", message = "feedDB: success, size: ${list.size}")
                UiState.Success(list)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun onFavoriteToggled(wikiModel: WikiModel) {
        viewModelScope.launch {
            Logger.d(tag = "FeedViewModel", message = "toggleFavorite, $wikiModel")
            wikiRepository.toggleFavorite(wikiModel)
        }
    }

    init {
        Logger.d(tag = "FeedViewModel", message = "onCreated")
        viewModelScope.launch {
            wikiRepository.observableRemoteWikiFeed
                .collect {
                    Logger.d(message = "collect remote feed")
                    isRefreshing = false
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(tag = "FeedViewModel", message = "onCleared")
    }
}