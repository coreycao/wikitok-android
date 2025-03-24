package com.sy.wikitok.di

import com.sy.wikitok.ui.screen.FavoriteViewModel
import com.sy.wikitok.ui.screen.FeedViewModel
import com.sy.wikitok.ui.screen.MainViewModel
import com.sy.wikitok.ui.screen.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 *
 * viewModel module
 *
 */

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { FeedViewModel(get()) }
    viewModel { FavoriteViewModel(get()) }
    viewModel { SearchViewModel(get()) }
}