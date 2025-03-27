package com.sy.wikitok.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FavoriteViewModel(private val wikiRepo: WikiRepository) : ViewModel() {

    fun deleteFavorite(wikiModel: WikiModel) {
        viewModelScope.launch {
            wikiRepo.removeFavAndUpdateFeed(wikiModel)
        }
    }

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


    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            _searchQuery.update { query }
        }
    }

    fun toggleSearchBar(isActive: Boolean){
        Logger.d(tag = "searchFlow", message = "toggleSearchBar $isActive")
        _isSearching.value = isActive
    }

    @OptIn(FlowPreview::class)
    val searchResult: StateFlow<UiState> =
        _searchQuery
            .onEach { query->
                Logger.d(tag = "searchFlow", message = "before filter, query $query")
            }
            .dropWhile { query ->
                Logger.d(tag = "searchFlow", message = "drop blank query")
                query.isBlank()
            }
            .onEach { query->
                Logger.d(tag = "searchFlow", message = "after filter, query $query")
            }
            .debounce(350)
            .distinctUntilChanged()
            .map { query ->
                if (query.isBlank()){
                    Logger.d(tag = "searchFlow", message = "onMap, blank query")
                    UiState.Success(emptyList())
                } else {
                    wikiRepo.searchFavorites(query).fold(
                        onSuccess = {
                            Logger.d(tag = "searchFlow", message = "onMap, fold ${it.size}")
                            UiState.Success(it)
                        },
                        onFailure = {
                            Logger.e(tag = "searchFlow", message = "catch ${it.message}")
                            UiState.Error(it.message ?: "Unknown Error")
                        }
                    )
                }
            }
            .catch {
                Logger.e(tag = "searchFlow", message = "catch ${it.message}")
                emit(UiState.Error(it.message ?: "Unknown Error"))
            }
            .stateIn(
                scope = viewModelScope,
                initialValue = UiState.Success(emptyList()),
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
            )

}