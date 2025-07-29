package com.sy.wikitok.data.model

import kotlinx.serialization.Serializable

/**
 * @author Yeung
 * @date 2025/7/27
 */
@Serializable
data class RemoteConfig(
    val generateAI: GenerateAI,
    val languages: List<Language>
)

@Serializable
data class GenerateAI(
    val enable: Boolean,
    val host: String,
    val model: String,
    val prompts: List<String>
)

@Serializable
data class Language(
    val id: String,
    val name: String,
    val flag: String,
    val api: String,
    val default: Boolean
)