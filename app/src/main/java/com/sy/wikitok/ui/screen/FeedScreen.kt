package com.sy.wikitok.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.BuildConfig
import com.sy.wikitok.ui.component.WikiPage
import com.sy.wikitok.ui.screen.FeedViewModel.UiState
import com.sy.wikitok.utils.Logger

/**
 * @author Yeung
 * @date 2025/3/24
 */
@Composable
fun FeedScreen(feedViewModel: FeedViewModel) {

    val feedUiState by feedViewModel.feedUiState.collectAsStateWithLifecycle()

    when (feedUiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(
            errorMessage = if (BuildConfig.DEBUG)
                (feedUiState as UiState.Error).message else null
        )

        is UiState.Empty -> EmptyScreen()
        is UiState.Success -> {
            val wikiList = (feedUiState as UiState.Success).wikiList
            val pagerState = rememberPagerState(initialPage = feedViewModel.currentPage) {
                wikiList.size
            }

            LaunchedEffect(pagerState.currentPage) {
                Logger.d(
                    tag = "FeedScreen",
                    message = "update currentPage: ${pagerState.currentPage}"
                )
                feedViewModel.currentPage = pagerState.currentPage
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
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
