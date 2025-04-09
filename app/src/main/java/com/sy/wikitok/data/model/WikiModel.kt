package com.sy.wikitok.data.model

import com.sy.wikitok.data.db.FavoriteEntity
import com.sy.wikitok.data.db.WikiEntity
import kotlinx.serialization.Serializable

/**
 * @author Yeung
 * @date 2025/3/18
 */
@Serializable
data class WikiModel(
    val id: String,
    val title: String,
    val content: String,
    val imgUrl: String,
    val linkUrl: String,
    val isFavorite: Boolean = false
)

fun WikiEntity.toWikiModel(): WikiModel = with(this) {
    WikiModel(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl,
        isFavorite = isFavorite
    )
}

fun FavoriteEntity.toWikiModel(): WikiModel = with(this) {
    WikiModel(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl,
        isFavorite = true
    )
}

fun WikiModel.toFeedEntity(): WikiEntity = with(this) {
    WikiEntity(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl,
        isFavorite = isFavorite
    )
}

fun WikiModel.toFavoriteEntity(): FavoriteEntity = with(this) {
    FavoriteEntity(
        id = id,
        title = title,
        content = content,
        imgUrl = imgUrl,
        linkUrl = linkUrl
    )
}