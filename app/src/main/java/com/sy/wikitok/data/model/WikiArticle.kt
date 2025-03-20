package com.sy.wikitok.data.model

/**
 * @author Yeung
 * @date 2025/3/18
 *
 * Map WikiResponse to WikiArticle
 * WikiArticle is only used in view layer
 *
 */
data class WikiArticle(
    val id: Int,
    val title: String,
    val content: String,
    val coverUrl: String,
)