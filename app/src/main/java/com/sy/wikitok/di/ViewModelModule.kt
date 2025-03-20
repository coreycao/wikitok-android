package com.sy.wikitok.di

import com.sy.wikitok.ui.screen.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 *
 * viewModel module:
 * provide MainViewModel
 */

val viewModelModule = module {
    viewModel {
        MainViewModel(get())
    }
}