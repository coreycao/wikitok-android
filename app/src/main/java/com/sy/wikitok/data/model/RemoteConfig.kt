package com.sy.wikitok.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Yeung
 * @date 2025/7/27
 */
@Serializable
data class RemoteConfig(
    val generateAI: GenerateAI,
    val languages: List<Language>,
)

@Serializable
data class GenerateAI(
    val summaryAI: SummaryAI,
    val chatAI: ChatAI
)

@Serializable
data class SummaryAI(
    val enable: Boolean,
    val host: String,
    val model: String,
    @SerialName("system_prompt")
    val systemPrompt: String,
    @SerialName("biz_prompt")
    val bizPrompt: String
)

@Serializable
data class ChatAI(
    val enable: Boolean,
    val host: String,
    val model: String,
    @SerialName("system_prompt")
    val systemPrompt: String,
    @SerialName("biz_prompt")
    val bizPrompt: String
)