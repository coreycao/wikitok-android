package com.sy.wikitok.data.repository

import android.util.Log
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.model.toFavoriteEntity
import com.sy.wikitok.data.model.toFeedEntity
import com.sy.wikitok.data.repository.WikiRepository.RepoState.Initial
import com.sy.wikitok.network.ApiService
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
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

    val feedFlow = MutableStateFlow<RepoState>(Initial)

    val favoriteUpdates = favDao.observeFavorites()

    suspend fun loadFeedData() {
        MockDataProvider().mockList().fold(
            onSuccess = { list ->
                feedFlow.update {
                    RepoState.Success(list)
                }
                saveWikiList(list)
            },
            onFailure = { error ->
                Log.e("WikiRepository", "loadFeedData: ${error.message}")
                getLocalWikiList().fold(
                    onSuccess = { list ->
                        feedFlow.update {
                            RepoState.Success(list)
                        }
                    },
                    onFailure = { errorLocal ->
                        Log.e("WikiRepository", "loadFeedData: ${errorLocal.message}")
                        feedFlow.update {
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

        val currentFeedList = (feedFlow.value as? RepoState.Success)?.list?.toMutableList()
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
            feedFlow.update {
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

        val currentFeedList = (feedFlow.value as? RepoState.Success)?.list?.toMutableList()
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
            feedFlow.update {
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
}