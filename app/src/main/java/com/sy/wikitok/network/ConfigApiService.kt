package com.sy.wikitok.network

import io.ktor.client.HttpClient

/**
 * @author Yeung
 * @date 2025/7/27
 */
class ConfigApiService(private val httpClient: HttpClient): BaseApiService {

    companion object {
        const val ENDPOINT = "https://raw.githubusercontent.com/coreycao/wikitok-android/main/backend/remote_config.json"
    }

    fun fetchRemoteConfig() {


    }
}