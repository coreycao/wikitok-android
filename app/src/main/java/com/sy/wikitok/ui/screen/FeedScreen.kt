package com.sy.wikitok.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.ui.component.WikiPage
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import com.sy.wikitok.utils.Logger
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/3/24
 */
@Composable
fun FeedScreen() {

    val feedViewModel = koinViewModel<FeedViewModel>()

    LaunchedEffect(Unit) {
        Logger.d(tag = "FeedScreen", message = "loadFeedData")
        feedViewModel.loadFeedData()
    }

    val feedUiState by feedViewModel.feedUiState.collectAsStateWithLifecycle()

    when (feedUiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen()
        is UiState.Success -> {
            val wikiList = (feedUiState as UiState.Success).wikiList
            if (wikiList.isEmpty()) {
                EmptyScreen()
            } else {

                val pagerState = rememberPagerState(initialPage = feedViewModel.currentPage) {
                    wikiList.size
                }

                LaunchedEffect(pagerState.currentPage) {
                    feedViewModel.currentPage = pagerState.currentPage
                }

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize(),
                ) { page ->
                    val wikiModel = wikiList[page]
                    WikiPage(
                        wikiModel = wikiModel,
                        onFavIconTapped = {
                            feedViewModel.onFavoriteToggled(wikiModel)
                        },
                        onDoubleTab = {
                            feedViewModel.onFavoriteToggled(wikiModel)
                        }
                    )

                }
            }
        }
    }
}
