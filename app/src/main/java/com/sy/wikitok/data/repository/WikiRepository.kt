package com.sy.wikitok.data.repository

import com.sy.wikitok.data.Language
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.db.WikiEntity
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.model.toFavoriteEntity
import com.sy.wikitok.network.WikiApiService
import kotlinx.coroutines.flow.map

/**
 * @author Yeung
 * @date 2025/3/20
 */
class WikiRepository(
    private val wikiApiService: WikiApiService,
    private val userRepository: UserRepository,
    private val feedDao: FeedDao,
    private val favDao: FavoriteDao
) {

    val observableFeed = feedDao.observerFeeds().map { entities ->
        entities.map { entity ->
            entity.toWikiModel()
        }
    }

    val favoriteUpdates = favDao.observeFavorites().map { entities ->
        entities.map { entity ->
            entity.toWikiModel()
        }
    }

    fun observableRemoteFeed(language: Language) = wikiApiService.observerWikiList(language.api)

    suspend fun saveAndMergeWikiList(list: List<WikiModel>) {
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
}