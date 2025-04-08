package com.sy.wikitok.di

import com.sy.wikitok.network.AppUpdateApiService
import com.sy.wikitok.network.WikiApiService
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
    single { provideWikiApiService(get()) }
    single { provideAppUpdateApiService(get()) }
}

fun provideHttpClient(): HttpClient {
    return httpClientAndroid
}

fun provideWikiApiService(httpClient: HttpClient): WikiApiService {
    return WikiApiService(httpClient)
}

fun provideAppUpdateApiService(httpClient: HttpClient): AppUpdateApiService {
    return AppUpdateApiService(httpClient)
}