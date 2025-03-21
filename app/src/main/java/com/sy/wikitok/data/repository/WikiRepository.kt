package com.sy.wikitok.data.repository

import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiArticle
import com.sy.wikitok.data.model.toArticle
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
    suspend fun getRemoteWikiList(): Result<List<WikiArticle>> {
        return runCatching {
            apiService.requestWikiList()
                .body<WikiApiResponse>()
                .query.pages.filter {
                    it.value.thumbnail != null
                }.map {
                    it.value.toArticle()
                }
        }
    }

    suspend fun saveWikiList(list: List<WikiArticle>) {
        feedDao.replaceAllFeeds(list.map {
            it.toFeedEntity()
        })
    }

    suspend fun getLocalWikiList(): Result<List<WikiArticle>> {
        return runCatching {
            feedDao.readFeeds().map {
                it.toArticle()
            }
        }
    }

    suspend fun toggleFavorite(wikiArticle: WikiArticle) {
        feedDao.updateFavorite(wikiArticle.id, !wikiArticle.isFavorite)
    }

    suspend fun addFavorite(wikiArticle: WikiArticle) {
        favDao.upsertFavorite(wikiArticle.toFavoriteEntity())
    }

    suspend fun removeFavorite(wikiArticle: WikiArticle) {
        favDao.removeFavorite(wikiArticle.id)
    }

    suspend fun getFavoriteList(): Result<List<WikiArticle>> {
        return runCatching {
            favDao.readAllFavorites().map {
                it.toArticle()
            }
        }
    }
}