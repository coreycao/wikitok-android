package com.sy.wikitok.network

import com.sy.wikitok.data.model.AppUpdateInfo
import com.sy.wikitok.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * @author Yeung
 * @date 2025/4/2
 */
class AppUpdateApiService(private val httpClient: HttpClient) : BaseApiService {

    companion object {
        private const val ENDPOINT = "https://gist.githubusercontent.com/coreycao/" +
                "299613c63de4da93c24dde499937bcc6/raw/1e837d3751fca572c274b5ffd214aa76e2bf7fe9/json"
    }

    /**
     * 请求版本信息
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun observerVersionInfo() = flow {
        emit(
            runCatching {
                val response = httpClient.get(ENDPOINT)
                val responseBody = response.bodyAsText()
                val json = Json {
                    allowTrailingComma = true
                }
                val appUpdateInfo = json.decodeFromString<AppUpdateInfo>(responseBody)
                Logger.i(
                    tag = "AppUpdateApiService",
                    message = "requestVersionInfo success"
                )
                appUpdateInfo
            }
        )
    }
}