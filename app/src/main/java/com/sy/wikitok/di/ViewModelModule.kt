package com.sy.wikitok.di

import com.sy.wikitok.ui.screen.ChatViewModel
import com.sy.wikitok.ui.screen.FavoriteViewModel
import com.sy.wikitok.ui.screen.FeedViewModel
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
    viewModel { SettingViewModel(get(), get()) }
    viewModel { FeedViewModel(get(), get()) }
    viewModel { FavoriteViewModel(get(), get()) }
    viewModel { ChatViewModel(get()) }
}