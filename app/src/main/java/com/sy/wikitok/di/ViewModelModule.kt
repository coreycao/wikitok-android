package com.sy.wikitok.di

import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.screen.ChatViewModel
import com.sy.wikitok.ui.screen.FavoriteViewModel
import com.sy.wikitok.ui.screen.FeedViewModel
import com.sy.wikitok.ui.screen.MainViewModel
import com.sy.wikitok.ui.screen.SettingViewModel
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

    viewModel { FeedViewModel(get(), get()) }
    viewModel { FavoriteViewModel(get(), get()) }
    viewModel { SettingViewModel(get(), get()) }

    viewModel { (wikiModel: WikiModel)-> ChatViewModel(get(), wikiModel) }
}