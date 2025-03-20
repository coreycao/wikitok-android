package com.sy.wikitok.di

import com.sy.wikitok.data.repository.RemoteRepositoryImpl
import com.sy.wikitok.network.ApiService
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 *
 * repository module:
 * provide remote repository
 */

val repositoryModule = module {
    single { provideRemoteRepository(get()) }
}

fun provideRemoteRepository(apiService: ApiService): RemoteRepositoryImpl {
    return RemoteRepositoryImpl(apiService)
}