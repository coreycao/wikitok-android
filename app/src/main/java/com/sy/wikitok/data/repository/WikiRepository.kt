package com.sy.wikitok.data.repository

import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.model.toWikiModel
import com.sy.wikitok.data.model.toFavoriteEntity
import com.sy.wikitok.data.model.toFeedEntity
import com.sy.wikitok.network.ApiService
import io.ktor.client.call.body

/**
 * @author Yeung
 * @date 2025/3/20
 */
class WikiRepository(
    private val apiService: ApiService,
    private val feedDao: FeedDao,
    private val favDao: FavoriteDao
) {
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
    }

    suspend fun addFavorite(wikiModel: WikiModel) {
        favDao.upsertFavorite(wikiModel.toFavoriteEntity())
    }

    suspend fun removeFavorite(wikiModel: WikiModel) {
        favDao.removeFavorite(wikiModel.id)
    }

    suspend fun getFavoriteList(): Result<List<WikiModel>> {
        return runCatching {
            favDao.readAllFavorites().map {
                it.toWikiModel()
            }
        }
    }

    val favoriteUpdates = favDao.observeFavorites()
}