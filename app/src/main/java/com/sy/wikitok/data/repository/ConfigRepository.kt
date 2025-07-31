package com.sy.wikitok.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sy.wikitok.data.db.LangDao
import com.sy.wikitok.data.model.ChatAI
import com.sy.wikitok.data.model.SummaryAI
import com.sy.wikitok.data.model.toEntity
import com.sy.wikitok.network.ConfigApiService
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * @author Yeung
 * @date 2025/7/24
 */
class ConfigRepository(
    private val configApi: ConfigApiService,
    private val configDataStore: DataStore<Preferences>,
    private val langDao: LangDao
) {

    private val chatAIKey = stringPreferencesKey("chatAI")

    private val summaryAIKey = stringPreferencesKey("summaryAI")

    // default config for genAI
    companion object {
        const val MODEL_ID = "THUDM/GLM-4.1V-9B-Thinking"
        const val AI_HOST = "https://api.siliconflow.cn/v1/"
        const val SUMMARY_SYSTEM_PROMPT = """
            你是一名小助手，善于洞察和总结。
            在接下来的对话中，请你根据我的问题，给出答案。
            """
        const val CHAT_SYSTEM_PROMPT = """
            你是一名 AI 智能助理，拥有丰富的百科知识，并且擅长耐心地向他们讲解相关的知识。
            在接下来的对话中，请你根据用户的问题，给出耐心的回复。
            对于不清楚、不知道的东西，你一律回答不知道就好。
        """
        const val SUMMARY_BIZ_PROMPT = """
            我将给出你一份我在浏览维基百科时收藏的词条清单，
            请你基于这些词条，总结出一份知识体系，谈谈你对我的洞察和了解。
            我收藏的词条如下：
        """
    }

    suspend fun readChatAIConfig(): ChatAI {
        val cache = configDataStore.data.first()[chatAIKey]
        return if (cache != null) {
            Json.decodeFromString<ChatAI>(cache)
        } else {
            ChatAI(true, AI_HOST, MODEL_ID, CHAT_SYSTEM_PROMPT, "")
        }
    }

    suspend fun readSummaryAIConfig(): SummaryAI {
        return withContext(Dispatchers.IO) {
            val cache = configDataStore.data.first()[summaryAIKey]
            if (cache != null) {
                Json.decodeFromString<SummaryAI>(cache)
            } else {
                SummaryAI(true, AI_HOST, MODEL_ID, SUMMARY_SYSTEM_PROMPT, SUMMARY_BIZ_PROMPT)
            }
        }
    }

    suspend fun fetchRemoteConfig() {
        configApi.fetchRemoteConfig().first()
            .fold(
                onSuccess = { config ->
                    Logger.i("fetchRemoteConfig success")
                    configDataStore.edit { preferences ->
                        Logger.i("save ai config to datastore")
                        preferences[chatAIKey] = Json.encodeToString(config.generateAI.chatAI)
                        preferences[summaryAIKey] = Json.encodeToString(config.generateAI.summaryAI)
                    }

                    Logger.i("save languages' config to db")
                    langDao.saveAndMergeLanguages(config.languages.map { model ->
                        model.toEntity()
                    })
                },
                onFailure = {
                    Logger.e("fetchRemoteConfig error: ${it.message}")
                }
            )
    }
}

