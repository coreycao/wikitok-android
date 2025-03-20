package com.sy.wikitok.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Yeung
 * @date 2025/3/18
 *
 */

@Serializable
data class WikiApiResponse(
    val batchcomplete: String,
    val `continue`: Continue,
    val query: Query
)

@Serializable
data class Continue(
    val grncontinue: String,
    val `continue`: String
)

@Serializable
data class Query(
    val pages: Map<String, Page>
)

@Serializable
data class Page(
    val pageid: Int,
    val ns: Int,
    val title: String,
    val extract: String,
    val contentmodel: String,
    val pagelanguage: String,
    val pagelanguagehtmlcode: String,
    val pagelanguagedir: String,
    val touched: String,
    val lastrevid: Int,
    val length: Int,
    val fullurl: String,
    val editurl: String,
    val canonicalurl: String,
    val varianttitles: Map<String, String>,
    // thumbnail may be empty
    @SerialName("thumbnail")
    val thumbnail: Thumbnail? = null
)

@Serializable
data class Thumbnail(
    val source: String,
    val width: Int,
    val height: Int
)