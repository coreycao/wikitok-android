package com.sy.wikitok.ui.screen

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.sy.wikitok.data.repository.WikiRepository.RepoState
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FeedViewModel(private val wikiRepository: WikiRepository) : ViewModel() {

    // keep view pager state
    var currentPage by mutableIntStateOf(0)

    private val _feedUiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val feedUiState = _feedUiState.asStateFlow()

    init {
        Logger.d(tag = "FeedViewModel", message = "init")
        viewModelScope.launch {
            wikiRepository.feedFlow.map {
                when (it) {
                    is RepoState.Success -> {
                        UiState.Success(it.list)
                    }

                    is RepoState.Failure -> {
                        UiState.Error(it.error)
                    }

                    is RepoState.Initial -> {
                        currentPage = 0
                        UiState.Loading
                    }
                }
            }.collect { mappedState ->
                _feedUiState.update {
                    mappedState
                }
            }
        }
    }

    fun loadFeedData() {
        viewModelScope.launch {
            wikiRepository.loadFeedData()
        }
    }

    fun onFavoriteToggled(wikiModel: WikiModel) {
        viewModelScope.launch {
            // toggle favorite
            wikiRepository.toggleFavorite(wikiModel)

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