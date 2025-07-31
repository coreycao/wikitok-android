package com.sy.wikitok.data.model

import com.sy.wikitok.data.db.LanguageEntity
import kotlinx.serialization.Serializable

/**
 * @author Yeung
 * @date 2025/3/29
 */

@Serializable
data class Language(
    val id: String,
    val name: String,
    val flag: String,
    val api: String,
    val selected: Boolean
)

fun Language.toEntity() = LanguageEntity(
    id = id,
    name = name,
    flag = flag,
    api = api,
    selected = if (selected) 1 else 0
)

fun LanguageEntity.toModel() = Language(
    id = id,
    name = name,
    flag = flag,
    api = api,
    selected = selected == 1
)

fun defaultLanguage() = Language(
    "zh-cn",
    "中文（中国大陆）",
    "https://hatscripts.github.io/circle-flags/flags/cn.svg",
    "https://zh.wikipedia.org/w/api.php",
    selected = true
)