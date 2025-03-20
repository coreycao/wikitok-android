package com.sy.wikitok.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiArticle
import com.sy.wikitok.data.repository.RemoteRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/3/20
 */
class MainViewModel(private val remoteRepo: RemoteRepositoryImpl) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState.Loading)
    val uiState = _uiState

    fun loadWikiList() {
        viewModelScope.launch {
            remoteRepo.getWikiList().fold(
                onSuccess = { list ->
                    Log.e("MainViewModel", "loadWikiList success, counts: ${list.size}")
                    _uiState.update {
                        if (list.isEmpty()) {
                            MainUiState.Error("No data")
                        } else {
                            MainUiState.Success(list)
                        }
                    }
                },
                onFailure = { e ->
                    Log.e("MainViewModel", "loadWikiList: ${e.message}")
                    _uiState.update {
                        MainUiState.Error(e.message ?: "Unknown Error")
                    }
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