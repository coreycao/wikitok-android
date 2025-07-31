package com.sy.wikitok.data.repository

import android.content.res.AssetManager
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.db.LangDao
import com.sy.wikitok.data.db.WikiEntity
import com.sy.wikitok.data.model.defaultLanguage
import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.model.toFavoriteEntity
import com.sy.wikitok.data.model.toWikiModelList
import com.sy.wikitok.data.model.toModel
import com.sy.wikitok.network.WikiApiService
import com.sy.wikitok.utils.Logger
import com.sy.wikitok.utils.loadJsonFromAssetsAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json

/**
 * @author Yeung
 * @date 2025/3/20
 */
class WikiRepository(
    private val wikiApiService: WikiApiService,
    private val feedDao: FeedDao,
    private val favDao: FavoriteDao,
    private val langDao: LangDao,
    private val assets: AssetManager
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val observableRemoteWikiFeed = observeLanguageSetting()
        .distinctUntilChanged()
        .flatMapLatest { language ->
            Logger.d("Language Changed: ${language.name}")
            wikiApiService.observerWikiList(language.api)
                .onEach {
                    if (it.isSuccess) {
                        Logger.d(tag = "WikiRepository", message = "feedRemote: success")
                        val list = it.getOrThrow().toWikiModelList()
                        Logger.d(
                            tag = "WikiRepository",
                            message = "feedRemote: success, count: ${list.size}"
                        )
                        saveAndMergeWikiList(list)
                    } else {
                        Logger.e(
                            tag = "WikiRepository",
                            message = "feedRemote, failure: ${it.exceptionOrNull()}"
                        )
                    }
                }
        }

    private fun observeLanguageSetting() = langDao.observeSelectedLanguage()
        .map { entity ->
            entity?.toModel() ?: defaultLanguage()
        }

    companion object {
        const val WIKI_ASSET = "wiki.json"
    }

    fun readFeedFromAsset(): Flow<Result<WikiApiResponse>> {
        return assets.loadJsonFromAssetsAsFlow(WIKI_ASSET)
            .map { jsonString ->
                delay(5000)
                val json = Json { ignoreUnknownKeys = true }
                val wikiResponse = json.decodeFromString<WikiApiResponse>(jsonString ?: "")
                Result.success(wikiResponse)
            }
    }

    val observableFeed = feedDao.observerFeeds().map { entities ->
        entities.map { entity ->
            entity.toWikiModel()
        }
    }

    private suspend fun saveAndMergeWikiList(list: List<WikiModel>) {
        val favoriteSet = favDao.readAllFavorites().map {
            it.id
        }.toSet()

        feedDao.replaceAllFeeds(list.map<WikiModel, WikiEntity> { item ->
            with(item) {
                WikiEntity(
                    id = id,
                    title = title,
                    content = content,
                    imgUrl = imgUrl,
                    linkUrl = linkUrl,
                    isFavorite = favoriteSet.contains(item.id)
                )
            }
        })
    }

    suspend fun toggleFavorite(wikiModel: WikiModel) {
        feedDao.updateFavorite(wikiModel.id, !wikiModel.isFavorite)
        if (wikiModel.isFavorite) {
            // the item onTapped is already in the favorite list
            // remove it from the favorite list
            favDao.removeFavorite(wikiModel.id)
        } else {
            // the item onTapped is not in the favorite list
            // add it to the favorite list
            favDao.upsertFavorite(wikiModel.toFavoriteEntity())
        }
    }

    val favoriteUpdates = favDao.observeFavorites().map { entities ->
        entities.map { entity ->
            entity.toWikiModel()
        }
    }

    suspend fun removeFavAndUpdateFeed(wikiModel: WikiModel) {
        favDao.removeFavorite(wikiModel.id)
        feedDao.updateFavorite(wikiModel.id, false)
    }

    fun observerSearchResult(keyword: String) =
        favDao.observerFavoritesCaseInsensitive(keyword).map { entities ->
            entities.map { entity ->
                entity.toWikiModel()
            }
        }

    suspend fun readLocalFavorites() = favDao.readAllFavorites()
        .map { entity -> entity.toWikiModel() }
}