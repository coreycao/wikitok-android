package com.sy.wikitok.data.model

import com.sy.wikitok.data.db.FavoriteEntity
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
    val imgUrl: String,
    val linkUrl: String,
    val isFavorite: Boolean = false
)

fun WikiEntity.toArticle(): WikiArticle = with(this) {
    WikiArticle(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl,
        isFavorite = isFavorite
    )
}

fun WikiArticle.toFeedEntity(): WikiEntity = with(this) {
    WikiEntity(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl,
        isFavorite = isFavorite
    )
}

fun FavoriteEntity.toArticle(): WikiArticle = with(this) {
    WikiArticle(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl,
        isFavorite = true
    )
}

fun WikiArticle.toFavoriteEntity(): FavoriteEntity = with(this) {
    FavoriteEntity(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl
    )
}