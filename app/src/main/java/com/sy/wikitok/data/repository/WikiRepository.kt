package com.sy.wikitok.data.repository

import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.model.toFavoriteEntity
import com.sy.wikitok.data.model.toFeedEntity
import com.sy.wikitok.data.repository.WikiRepository.RepoState.Initial
import com.sy.wikitok.network.ApiService
import com.sy.wikitok.utils.Logger
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * @author Yeung
 * @date 2025/3/20
 */
class WikiRepository(
    private val apiService: ApiService,
    private val feedDao: FeedDao,
    private val favDao: FavoriteDao
) {

    sealed class RepoState {
        data class Success(val list: List<WikiModel>) : RepoState()
        data class Failure(val error: String) : RepoState()
        data object Initial : RepoState()
    }

    private val _feedFlow = MutableStateFlow<RepoState>(Initial)

    val feedFlow = _feedFlow.asStateFlow()

    val favoriteUpdates = favDao.observeFavorites()

    suspend fun loadFeedData() {
//        MockDataProvider().mockList().fold(
        getRemoteWikiList().fold(
            onSuccess = { list ->
                Logger.d("loadFeedData success: $list", tag = "WikiRepo")
                _feedFlow.update {
                    RepoState.Success(list)
                }
                saveWikiList(list)
            },
            onFailure = { error ->
                getLocalWikiList().fold(
                    onSuccess = { list ->
                    Logger.d("loadLocalFeedData success: ${error.message}", tag = "WikiRepo")
                        _feedFlow.update {
                            RepoState.Success(list)
                        }
                    },
                    onFailure = { errorLocal ->
                        Logger.d(
                            tag = "WikiRepo",
                            message = "loadLocalFeedData failed: ${errorLocal.message}"
                        )
                        _feedFlow.update {
                            RepoState.Failure(errorLocal.message ?: "Unknown Error")
                        }
                    }
                )
            }
        )
    }

    suspend fun getRemoteWikiList(): Result<List<WikiModel>> {
        return runCatching {
            apiService.requestWikiList()
                .body<WikiApiResponse>()
                .query.pages.filter {
                    it.value.thumbnail != null
                }.map {
                    it.value.toWikiModel()
                }
        }
    }

    suspend fun saveWikiList(list: List<WikiModel>) {
        feedDao.replaceAllFeeds(list.map {
            it.toFeedEntity()
        })
    }

    suspend fun getLocalWikiList(): Result<List<WikiModel>> {
        return runCatching {
            feedDao.readFeeds().map {
                it.toWikiModel()
            }
        }
    }

    suspend fun toggleFavorite(wikiModel: WikiModel) {
        feedDao.updateFavorite(wikiModel.id, !wikiModel.isFavorite)

        val currentFeedList = (_feedFlow.value as? RepoState.Success)?.list?.toMutableList()
        currentFeedList?.find {
            it.id == wikiModel.id
        }?.let { item ->
            val updatedWikiModel = item.copy(isFavorite = !item.isFavorite)
            currentFeedList.replaceAll {
                if (it.id == updatedWikiModel.id) {
                    updatedWikiModel
                } else {
                    it
                }
            }
            _feedFlow.update {
                RepoState.Success(currentFeedList.toList())
            }
        }
    }

    suspend fun addFavorite(wikiModel: WikiModel) {
        favDao.upsertFavorite(wikiModel.toFavoriteEntity())
    }

    suspend fun removeFavorite(wikiModel: WikiModel) {
        favDao.removeFavorite(wikiModel.id)
    }

    suspend fun removeFavAndUpdateFeed(wikiModel: WikiModel) {
        favDao.removeFavorite(wikiModel.id)
        feedDao.updateFavorite(wikiModel.id, false)

        val currentFeedList = (_feedFlow.value as? RepoState.Success)?.list?.toMutableList()
        currentFeedList?.find {
            it.id == wikiModel.id
        }?.let { item ->
            val updatedWikiModel = item.copy(isFavorite = false)
            currentFeedList.replaceAll {
                if (it.id == updatedWikiModel.id) {
                    updatedWikiModel
                } else {
                    it
                }
            }
            _feedFlow.update {
                RepoState.Success(currentFeedList.toList())
            }
        }

    }

    suspend fun getFavoriteList(): Result<List<WikiModel>> {
        return runCatching {
            favDao.readAllFavorites().map {
                it.toWikiModel()
            }
        }
    }

    suspend fun searchFavorites(keyword: String): Result<List<WikiModel>> {
        return runCatching {
            favDao.searchFavoritesCaseInsensitive(keyword).map {
                it.toWikiModel()
            }
        }
    }
}