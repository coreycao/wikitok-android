package com.sy.wikitok.ui.screen

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.WikiRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.repository.UserRepository
import com.sy.wikitok.utils.Logger
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlin.collections.map

/**
 * @author Yeung
 * @date 2025/3/22
 */
class FeedViewModel(
    private val wikiRepository: WikiRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // keep view pager state
    var currentPage by mutableIntStateOf(0)

    sealed class UiState {
        data class Success(val wikiList: List<WikiModel>) : UiState()
        data class Error(val message: String) : UiState()
        object Loading : UiState()
        object Empty : UiState()
    }

    fun initFeedData() {
        viewModelScope.launch {
            Logger.d(tag = "FeedViewModel", message = "collectDB")
            _feedDBFlow.collect()
        }
        viewModelScope.launch {
            Logger.d(tag = "FeedViewModel", message = "collectRemote")
            _feedRemoteFlow.collect()
        }
    }

    private val _feedUiState = MutableStateFlow<UiState>(UiState.Loading)
    val feedUiState = _feedUiState.asStateFlow()

    private val _feedDBFlow = wikiRepository.observableFeed
        .map { list ->
            if (list.isEmpty()) {
                Logger.d(tag = "FeedViewModel", message = "feedDB: empty")
                _feedUiState.value = UiState.Empty
            } else {
                Logger.d(tag = "FeedViewModel", message = "feedDB: success")
                _feedUiState.value = UiState.Success(list)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Unit
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _feedRemoteFlow = userRepository.observeLanguageSetting()
        .flatMapLatest { lang ->
            Logger.i(tag = "FeedViewModel", message = "language: ${lang.name}")
            wikiRepository.observableRemoteFeed(lang)
                .onStart {
                    Logger.d(tag = "FeedViewModel", message = "feedRemote: onStart")
                    _feedUiState.value = UiState.Loading
                }
                .map {
                    if (it.isSuccess) {
                        Logger.d(tag = "FeedViewModel", message = "feedRemote: success")
                        val list = it.getOrThrow().toWikiModelList()
                        Logger.d(
                            tag = "FeedViewModel",
                            message = "feedRemote: success, count: ${list.size}"
                        )
                        wikiRepository.saveAndMergeWikiList(list)
                    } else {
                        Logger.e(tag = "FeedViewModel", message = "feedRemote, failure: ${it.exceptionOrNull()}")
                        _feedUiState.value =
                            UiState.Error(it.exceptionOrNull()?.message ?: "Unknown error")
                    }
                }.catch {
                    Logger.e("FeedViewModel", "feedRemote, catch: ${it.message}")
                    _feedUiState.value = UiState.Error(it.message.toString())
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Unit
        )

    private suspend fun HttpResponse.toWikiModelList(): List<WikiModel> {
        return this.body<WikiApiResponse>()
            .query.pages
            .filter { it.value.thumbnail != null }
            .map { it.value.toWikiModel() }
    }

    fun onFavoriteToggled(wikiModel: WikiModel) {
        viewModelScope.launch {
            wikiRepository.toggleFavorite(wikiModel)
        }
    }
}