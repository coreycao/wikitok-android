package com.sy.wikitok.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.component.WikiPage
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/3/24
 */
@Composable
fun FeedScreen(
    feedUiState: UiState,
    onFavoriteToggled: (WikiModel) -> Unit,
) {
    val viewModel: FeedViewModel = koinViewModel()

    when (feedUiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen()
        is UiState.Success -> {
            val wikiList = feedUiState.wikiList
            if (wikiList.isEmpty()) {
                EmptyScreen()
            } else {

                val pagerState = rememberPagerState(initialPage = viewModel.currentPage) {
                    wikiList.size
                }

                LaunchedEffect(pagerState.currentPage) {
                    viewModel.currentPage = pagerState.currentPage
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
                            onFavoriteToggled(wikiModel)
                        },
                        onDoubleTab = {
                            onFavoriteToggled(wikiModel)
                        }
                    )

                }
            }
        }
    }
}
