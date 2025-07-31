package com.sy.wikitok.network

import com.sy.wikitok.data.model.RemoteConfig
import com.sy.wikitok.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * @author Yeung
 * @date 2025/7/27
 */
class ConfigApiService(private val httpClient: HttpClient): BaseApiService {

    companion object {
        const val ENDPOINT = "https://raw.githubusercontent.com/coreycao/wikitok-android/main/backend/remote_config.json"
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun fetchRemoteConfig() = flow {
        emit(
            runCatching {
                val response = httpClient.get(ENDPOINT)
                val responseBody = response.bodyAsText()
                val json = Json {
                    allowTrailingComma = true
                }
                val remoteConfig = json.decodeFromString<RemoteConfig>(responseBody)
                Logger.i("fetch remote config success")
                remoteConfig
            }.onFailure {
                Logger.e("fetch remote config error: ${it.message}")
            }
        )
    }
}