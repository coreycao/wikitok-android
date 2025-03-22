package com.sy.wikitok.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FavoriteViewModel(private val wikiRepo: WikiRepository) : ViewModel() {

    val favorites = wikiRepo.favoriteUpdates
        .map { entities ->
            entities.map {
                it.toWikiModel()
            }
        }
        .map<List<WikiModel>, UiState> { list ->
            UiState.Success(list)
        }
        .onEach {
            Log.d("FavoriteViewModel", "favorites: $it")
        }
        .catch { e ->
            Log.e("FavoriteViewModel", "favorites: ${e.message}")
            emit(UiState.Error(e.message ?: "Unknown Error"))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            UiState.Loading
        )

}