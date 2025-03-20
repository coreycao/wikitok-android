package com.sy.wikitok.data.repository

import com.sy.wikitok.data.model.WikiArticle

/**
 * @author Yeung
 * @date 2025/3/20
 */
interface RemoteRepository {
    suspend fun getWikiList(): Result<List<WikiArticle>>
}