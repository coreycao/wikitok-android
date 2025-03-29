package com.sy.wikitok.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sy.wikitok.data.Langs
import com.sy.wikitok.data.Language
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * @author Yeung
 * @date 2025/3/20
 */
class WikiRepository(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>,
    private val feedDao: FeedDao,
    private val favDao: FavoriteDao
) {

    sealed class RepoState {
        data class Success(val list: List<WikiModel>) : RepoState()
        data class Failure(val error: String) : RepoState()
        data object Initial : RepoState()
    }

    private val KEY_LANG = stringPreferencesKey("lang")
    val DEFAULT_LANG = "en"

    private val _feedFlow = MutableStateFlow<RepoState>(Initial)

    val feedFlow = _feedFlow.asStateFlow()

    val favoriteUpdates = favDao.observeFavorites()

    fun currentLang(): Flow<Language> {
        return dataStore.data.map { preference ->
            val currentLang = preference[KEY_LANG] ?: DEFAULT_LANG
            Langs[currentLang]!!
        }
    }

    suspend fun changeLanguage(lang: Language) {
        _feedFlow.update {
            Initial
        }
        dataStore.edit { preference ->
            preference[KEY_LANG] = lang.id
        }
    }

    suspend fun loadFeedData() {
        dataStore.data.map { preference ->
            val lang = preference[KEY_LANG] ?: DEFAULT_LANG
            val api = Langs[lang]?.api!!
            getRemoteWikiList(api).fold(
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
                            Logger.d(
                                "loadLocalFeedData success: ${error.message}",
                                tag = "WikiRepo"
                            )
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
        }.collect()
    }

    suspend fun getRemoteWikiList(api: String): Result<List<WikiModel>> {
        return runCatching {
            apiService.requestWikiList(api)
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