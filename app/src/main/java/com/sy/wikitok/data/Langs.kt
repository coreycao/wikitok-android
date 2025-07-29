package com.sy.wikitok.data

/**
 * @author Yeung
 * @date 2025/3/29
 */

const val DEFAULT_LANG_ID = "zh-cn"

data class Language(
    val id: String,
    val name: String,
    val flag: String,
    val api: String,
    val selected: Boolean
)

val Langs = mapOf(

    "zh-cn" to Language(
        "zh-cn",
        "中文（中国大陆）",
        "https://hatscripts.github.io/circle-flags/flags/cn.svg",
        "https://zh.wikipedia.org/w/api.php",
        selected = true
    ),

    "en" to Language(
        "en",
        "English",
        "https://hatscripts.github.io/circle-flags/flags/us.svg",
        "https://en.wikipedia.org/w/api.php",
        selected = false
    ),


    "ja" to Language(
        "ja",
        "日本語",
        "https://hatscripts.github.io/circle-flags/flags/jp.svg",
        "https://ja.wikipedia.org/w/api.php",
        selected = false
    )

)
