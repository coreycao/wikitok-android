package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FavoriteViewModel(private val wikiRepo: WikiRepository) : ViewModel() {

    sealed class UiState {
        data class Success(val wikiList: List<WikiModel>) : UiState()
        data class Error(val message: String) : UiState()
        data object Loading : UiState()
        data object Empty : UiState()
    }

    fun deleteFavorite(wikiModel: WikiModel) {
        viewModelScope.launch {
            wikiRepo.removeFavAndUpdateFeed(wikiModel)
        }
    }

    val favorites = wikiRepo.favoriteUpdates
        .map<List<WikiModel>, UiState> { list ->
            return@map if (list.isEmpty()) {
                Logger.d(tag = "FavoriteViewModel", message = "favorites: empty")
                UiState.Empty
            } else {
                Logger.d(tag = "FavoriteViewModel", message = "favorites counts: ${list.size}")
                UiState.Success(list)
            }
        }
        .catch { e ->
            Logger.e("FavoriteViewModel", "favorites: ${e.message}")
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

    fun toggleSearchBar(isActive: Boolean) {
        Logger.d(tag = "searchFlow", message = "toggleSearchBar $isActive")
        _isSearching.value = isActive
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResult: StateFlow<UiState> =
        _searchQuery
            .dropWhile { query ->
                Logger.d(tag = "searchFlow", message = "drop blank query")
                query.isBlank()
            }
            .debounce(350)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    Logger.d(tag = "searchFlow", message = "flatMapLatest, blank query")
                    flowOf(UiState.Empty)
                } else {
                    wikiRepo.observerSearchResult(query)
                        .map<List<WikiModel>, UiState> {
                            if (it.isEmpty()) {
                                Logger.d(tag = "searchFlow", message = "flatMapLatest#map, empty")
                                UiState.Empty
                            } else {
                                Logger.d(
                                    tag = "searchFlow",
                                    message = "flatMapLatest#map count: ${it.size}"
                                )
                                UiState.Success(it)
                            }
                        }
                        .onStart {
                            emit(UiState.Empty)
                        }
                }
            }
            .catch {
                Logger.e(tag = "searchFlow", message = "catch: ${it.message}")
                emit(UiState.Error(it.message ?: "Unknown Error"))
            }
            .stateIn(
                scope = viewModelScope,
                initialValue = UiState.Empty,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
            )

}