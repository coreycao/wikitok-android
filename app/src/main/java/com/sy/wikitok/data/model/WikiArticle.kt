package com.sy.wikitok.data.model

import com.sy.wikitok.data.db.WikiEntity

/**
 * @author Yeung
 * @date 2025/3/18
 *
 * Map WikiResponse to WikiArticle
 * WikiArticle is only used in view layer
 *
 */
data class WikiArticle(
    val id: String,
    val title: String,
    val content: String,
    val coverUrl: String,
    val articleUrl: String,
    val isFavorite: Boolean = false
)

fun WikiEntity.toArticle(): WikiArticle {
    return WikiArticle(
        id = id,
        title = title,
        content = content,
        coverUrl = imgUrl,
        articleUrl = linkUrl,
        isFavorite = isFavorite
    )
}

fun WikiArticle.toEntity(): WikiEntity {
    return WikiEntity(
        id = id,
        title = title,
        content = content,
        imgUrl = coverUrl,
        linkUrl = articleUrl,
        isFavorite = isFavorite
    )
}