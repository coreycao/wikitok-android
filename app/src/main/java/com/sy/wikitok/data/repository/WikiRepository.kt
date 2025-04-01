package com.sy.wikitok.data.repository

import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.model.toFavoriteEntity
import com.sy.wikitok.data.model.toFeedEntity
import com.sy.wikitok.network.ApiService
import com.sy.wikitok.utils.Logger
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * @author Yeung
 * @date 2025/3/20
 */
class WikiRepository(
    private val apiService: ApiService,
    private val userRepository: UserRepository,
    private val feedDao: FeedDao,
    private val favDao: FavoriteDao
) {

    sealed class RepoState {
        data class Success(val list: List<WikiModel>) : RepoState()
        data class Failure(val error: String) : RepoState()
        data object Initial : RepoState()
    }

    val favoriteUpdates = favDao.observeFavorites().map { entities ->
        entities.map { entity ->
            entity.toWikiModel()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val observableWikiRepo = userRepository.observeLanguageSetting()
        .flatMapLatest { lang ->
            Logger.d("getFeedFlow lang: ${lang.name}", tag = "WikiRepo")

            // TODO: merge fav status with local fav list.
            apiService.observerWikiList(lang.api)
                .map<Result<HttpResponse>, RepoState> { result ->
                    if (result.isSuccess) {
                        // request success, parse exception may happen here
                        Logger.d("getRemoteFeed success", tag = "WikiRepo")
                        val list = result.getOrThrow().toWikiModelList()
                        // trigger DB flow
                        saveWikiList(list)
                        RepoState.Success(list)
                    } else {
                        // local data my be empty
                        Logger.d(
                            tag = "WikiRepo",
                            message = "getFeedFlow failed: ${result.exceptionOrNull()?.message}"
                        )
                        val localFeedData = feedDao.readFeeds().map {
                            it.toWikiModel()
                        }
                        Logger.d(
                            tag = "WikiRepo",
                            message = "getLocalFeed success, count: ${localFeedData.size}"
                        )
                        RepoState.Success(localFeedData)
                    }

                }.onStart {
                    emit(RepoState.Initial)
                }
        }.catch { error ->
            Logger.d(
                tag = "WikiRepo",
                message = "getFeedFlow catch: ${error.message}"
            )
            emit(RepoState.Failure(error.message ?: "Unknown Error"))
        }


    private suspend fun HttpResponse.toWikiModelList(): List<WikiModel> {
        return this.body<WikiApiResponse>()
            .query.pages
            .filter { it.value.thumbnail != null }
            .map { it.value.toWikiModel() }
    }

    private suspend fun WikiApiResponse.toWikiModelList(): List<WikiModel> {
        return this
            .query.pages
            .filter { it.value.thumbnail != null }
            .map { it.value.toWikiModel() }
    }

    suspend fun saveWikiList(list: List<WikiModel>) {
        feedDao.replaceAllFeeds(list.map {
            it.toFeedEntity()
        })
    }

    suspend fun toggleFavorite(wikiModel: WikiModel): List<WikiModel> {
        return feedDao.updateGetFavorite(wikiModel.id, !wikiModel.isFavorite).map {
            it.toWikiModel()
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
        // TODO: notify feed list to update
        feedDao.updateFavorite(wikiModel.id, false)
    }

    fun observerSearchResult(keyword: String) =
        favDao.observerFavoritesCaseInsensitive(keyword).map { entities ->
            entities.map { entity ->
                entity.toWikiModel()
            }
        }
}