package com.sy.wikitok.di

import com.sy.wikitok.dataStore
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 */

val appModule = module {

    single { androidApplication().dataStore }

    includes(
        networkModule,
        roomModule,
        repositoryModule,
        viewModelModule
    )
}