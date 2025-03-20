package com.sy.wikitok.data.repository

import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiArticle
import com.sy.wikitok.network.ApiService
import io.ktor.client.call.body

/**
 * @author Yeung
 * @date 2025/3/20
 */
class RemoteRepositoryImpl(private val apiService: ApiService) : RemoteRepository {
    override suspend fun getWikiList(): Result<List<WikiArticle>> {
        return runCatching {
            apiService.requestWikiList()
                .body<WikiApiResponse>()
                .query
                .pages.filter {
                    it.value.thumbnail != null
                }.map {
                    WikiArticle(
                        id = it.value.pageid,
                        title = it.value.title,
                        content = it.value.extract,
                        coverUrl = it.value.thumbnail!!.source
                    )
                }
        }
    }
}