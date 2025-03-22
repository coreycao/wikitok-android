package com.sy.wikitok.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.component.FavItem
import com.sy.wikitok.ui.component.IconFavorite
import com.sy.wikitok.ui.component.IconHome
import com.sy.wikitok.ui.component.WikiPage
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import com.sy.wikitok.ui.theme.WikiTokTheme
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/3/19
 */

// Route
private const val ROUTE_FEED = "feed"
private const val ROUTE_FAVORITE = "favorite"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WikiTokTheme {

                val feedViewModel: FeedViewModel = koinViewModel()
                val feedUIState by feedViewModel.uiState.collectAsStateWithLifecycle()

                val favoriteViewModel: FavoriteViewModel = koinViewModel()
                val favoriteUIState by favoriteViewModel.favorites.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = Unit, block = {
                    feedViewModel.loadWikiList()
                })

                HomeScaffold(feedUIState, favoriteUIState)
            }
        }
    }
}

@Composable
private fun HomeScaffold(feedUIState: UiState, favoriteUIState: UiState) {
    val feedViewModel: FeedViewModel = koinViewModel()

    var currentRoute by rememberSaveable { mutableStateOf(ROUTE_FEED) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == ROUTE_FEED,
                    onClick = { currentRoute = ROUTE_FEED },
                    icon = {
                        Icon(
                            imageVector = IconHome,
                            contentDescription = stringResource(R.string.desc_nav_feed)
                        )
                    }

                )
                NavigationBarItem(
                    selected = currentRoute == ROUTE_FAVORITE,
                    onClick = { currentRoute = ROUTE_FAVORITE },
                    icon = {
                        Icon(
                            imageVector = IconFavorite,
                            contentDescription = stringResource(R.string.desc_nav_favorite)
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(bottom = innerPadding.calculateBottomPadding())

        ) {
            when (currentRoute) {
                ROUTE_FEED -> {
                    FeedScreen(
                        feedUIState,
                        onDoubleTab = feedViewModel::onDoubleTab
                    )
                }

                ROUTE_FAVORITE ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        FavoriteScreen(favoriteUIState)
                    }
            }
        }
    }
}

@Composable
private fun FeedScreen(
    feedUiState: UiState,
    onDoubleTab: (WikiModel) -> Unit,
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

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onDoubleTab(wikiModel)
                                    })
                            }) {
                        WikiPage(
                            title = wikiModel.title,
                            content = wikiModel.content,
                            imgUrl = wikiModel.imgUrl,
                            linkUrl = wikiModel.linkUrl,
                            isFavorite = wikiModel.isFavorite,
                        )

                    }
                }

            }
        }
    }
}

@Composable
private fun FavoriteScreen(favoriteUIState: UiState) {

    when (favoriteUIState) {
        is UiState.Loading -> LoadingScreen()

        is UiState.Error -> ErrorScreen()

        is UiState.Success -> {
            val favoriteList = favoriteUIState.wikiList
            if (favoriteList.isEmpty()) {
                EmptyScreen()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(favoriteList.size) { index ->
                        FavItem(
                            favoriteList[index],
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.txt_screen_empty))
    }
}

@Composable
private fun ErrorScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.txt_feed_error))
    }
}