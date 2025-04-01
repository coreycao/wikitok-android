package com.sy.wikitok.ui.screen

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.WikiRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.sy.wikitok.data.repository.WikiRepository.RepoState
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FeedViewModel(private val wikiRepository: WikiRepository) : ViewModel() {

    // keep view pager state
    var currentPage by mutableIntStateOf(0)

    sealed class UiState {
        data class Success(val wikiList: List<WikiModel>) : UiState()
        data class Error(val message: String) : UiState()
        data object Loading : UiState()
        data object Empty : UiState()
    }

    fun initFeedData() {
        viewModelScope.launch {
            Logger.d(tag = "FeedViewModel", message = "initFeedData")
            _wikiRepoState.collect()
        }
    }

    private val _feedUiState = MutableStateFlow<UiState>(UiState.Loading)
    val feedUiState = _feedUiState.asStateFlow()

    private val _wikiRepoState = wikiRepository.observableWikiRepo
        .map<RepoState, Unit> { state ->
            return@map when (state) {
                is RepoState.Success -> {
                    Logger.d(
                        tag = "FeedViewModel",
                        message = "feed: success, count: ${state.list.size}"
                    )
                    if (state.list.isEmpty()) {
                        _feedUiState.value = UiState.Empty
                    } else {
                        _feedUiState.value = UiState.Success(state.list)
                    }
                }

                is RepoState.Failure -> {
                    Logger.e("FeedViewModel", "feed: ${state.error}")
                    _feedUiState.value = UiState.Error(state.error)
                }

                is RepoState.Initial -> {
                    Logger.d(tag = "FeedViewModel", message = "feed: initial")
                    _feedUiState.value = UiState.Loading
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            UiState.Loading
        )

    fun onFavoriteToggled(wikiModel: WikiModel) {
        viewModelScope.launch {
            // toggle favorite
            val updatedList = wikiRepository.toggleFavorite(wikiModel)
            _feedUiState.value = UiState.Success(updatedList)

            if (wikiModel.isFavorite) {
                // the item onTapped is already in the favorite list
                // remove it from the favorite list
                wikiRepository.removeFavorite(wikiModel)
            } else {
                // the item onTapped is not in the favorite list
                // add it to the favorite list
                wikiRepository.addFavorite(wikiModel)
            }
        }
    }
}