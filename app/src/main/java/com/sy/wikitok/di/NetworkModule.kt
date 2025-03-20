package com.sy.wikitok.di

import com.sy.wikitok.network.ApiService
import com.sy.wikitok.network.httpClientAndroid
import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 *
 * network module:
 * provide apiService
 */

val networkModule = module {
    single { provideHttpClient() }
    single { provideApiService(get()) }
}

fun provideHttpClient(): HttpClient {
    return httpClientAndroid
}

fun provideApiService(httpClient: HttpClient): ApiService {
    return ApiService(httpClient)
}