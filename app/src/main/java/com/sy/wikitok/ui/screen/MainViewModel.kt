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
                            Log.d("MainViewModel", "loadLocalWikiList success, counts: ${list.size}")
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

    sealed class MainUiState {
        data object Loading : MainUiState()
        data class Success(val wikiList: List<WikiArticle>) : MainUiState()
        data class Error(val message: String) : MainUiState()
    }
}