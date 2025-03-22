package com.sy.wikitok.ui.screen

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.toList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FeedViewModel(private val wikiRepository: WikiRepository) : ViewModel() {

    // keep view pager state
    var currentPage by mutableIntStateOf(0)

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState = _uiState

    fun loadWikiList() {
        viewModelScope.launch {
            wikiRepository.getRemoteWikiList().fold(
                onSuccess = { list ->
                    Log.d("MainViewModel", "loadRemoteWikiList success, counts: ${list.size}")
                    _uiState.update {
                        if (list.isEmpty()) {
                            UiState.Error("No data")
                        } else {
                            UiState.Success(list)
                        }
                    }
                    // cache to local db
                    Log.d("MainViewModel", "saveWikiList to local db")
                    wikiRepository.saveWikiList(list)
                },
                onFailure = { errorRemote ->
                    Log.e("MainViewModel", "loadRemoteWikiList: ${errorRemote.message}")
                    wikiRepository.getLocalWikiList().fold(
                        onSuccess = { list ->
                            Log.d(
                                "MainViewModel",
                                "loadLocalWikiList success, counts: ${list.size}"
                            )
                            _uiState.update {
                                if (list.isEmpty()) {
                                    UiState.Error("No data")
                                } else {
                                    UiState.Success(list)
                                }
                            }
                        },
                        onFailure = { errorLocal ->
                            Log.e("MainViewModel", "loadLocalWikiList: ${errorLocal.message}")
                            _uiState.update {
                                UiState.Error(errorLocal.message ?: "Unknown Error")
                            }
                        }
                    )
                }
            )
        }
    }

    fun onDoubleTab(wikiModel: WikiModel) {
        viewModelScope.launch {
            val currentFeedList = (uiState.value as? UiState.Success)?.wikiList?.toMutableList()
            currentFeedList?.find {
                it.id == wikiModel.id
            }?.let { item ->
                wikiRepository.toggleFavorite(item)
                val updatedWikiModel = item.copy(isFavorite = !item.isFavorite)
                currentFeedList.replaceAll {
                    if (it.id == updatedWikiModel.id) {
                        updatedWikiModel
                    } else {
                        it
                    }
                }
                _uiState.update {
                    UiState.Success(currentFeedList.toList())
                }
            }

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