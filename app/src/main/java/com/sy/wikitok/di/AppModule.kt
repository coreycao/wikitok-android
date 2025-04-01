package com.sy.wikitok.di

import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 */

val appModule = module {
    includes(
        networkModule,
        roomModule,
        repositoryModule,
        viewModelModule
    )
}