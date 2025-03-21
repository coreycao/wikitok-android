package com.sy.wikitok.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiArticle
import com.sy.wikitok.data.repository.WikiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/3/20
 */
class MainViewModel(private val wikiRepository: WikiRepository) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState.Loading)
    val uiState = _uiState

    private val _favoriteListState: MutableStateFlow<List<WikiArticle>> =
        MutableStateFlow(emptyList())
    val favoriteListState = _favoriteListState

    fun loadWikiList() {
        viewModelScope.launch {
            wikiRepository.getRemoteWikiList().fold(
                onSuccess = { list ->
                    Log.d("MainViewModel", "loadRemoteWikiList success, counts: ${list.size}")
                    _uiState.update {
                        if (list.isEmpty()) {
                            MainUiState.Error("No data")
                        } else {
                            MainUiState.Success(list)
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
                                    MainUiState.Error("No data")
                                } else {
                                    MainUiState.Success(list)
                                }
                            }
                        },
                        onFailure = { errorLocal ->
                            Log.e("MainViewModel", "loadLocalWikiList: ${errorLocal.message}")
                            _uiState.update {
                                MainUiState.Error(errorLocal.message ?: "Unknown Error")
                            }
                        }
                    )
                }
            )
        }
    }

    fun loadFavoriteList() {
        viewModelScope.launch {
            wikiRepository.getFavoriteList().fold(
                onSuccess = { list ->
                    Log.d("MainViewModel", "loadFavoriteList success, counts: ${list.size}")
                    _favoriteListState.update {
                        list
                    }
                },
                onFailure = { error ->
                    Log.e("MainViewModel", "loadFavoriteList: ${error.message}")
                }
            )
        }
    }

    fun onDoubleTab(wikiArticle: WikiArticle) {
        viewModelScope.launch {
            val currentFeedList = (uiState.value as? MainUiState.Success)?.wikiList?.toMutableList()
            currentFeedList?.find {
                it.id == wikiArticle.id
            }?.let { article ->
                wikiRepository.toggleFavorite(article)
                val updatedArticle = article.copy(isFavorite = !article.isFavorite)
                currentFeedList.replaceAll {
                    if (it.id == updatedArticle.id) {
                        updatedArticle
                    } else {
                        it
                    }
                }
                _uiState.update {
                    MainUiState.Success(currentFeedList.toList())
                }
            }

            val currentFavoriteList = favoriteListState.value.toMutableList()
            if (wikiArticle.isFavorite) {
                // the item onTapped is already in the favorite list
                // remove it from the favorite list
                wikiRepository.removeFavorite(wikiArticle)
                // filter the item from the favorite list by id
                _favoriteListState.update {
                    currentFavoriteList.filter { it.id != wikiArticle.id }.toList()
                }
            } else {
                // the item onTapped is not in the favorite list
                // add it to the favorite list
                wikiRepository.addFavorite(wikiArticle)
                // before add the item to the favorite list, check if the item is already in the favorite list
                _favoriteListState.update {
                    currentFavoriteList.filter { it.id == wikiArticle.id }.plus(wikiArticle).toList()
                }

            }
        }
    }

    sealed class MainUiState {
        data object Loading : MainUiState()
        data class Success(val wikiList: List<WikiArticle>) : MainUiState()
        data class Error(val message: String) : MainUiState()
    }
}