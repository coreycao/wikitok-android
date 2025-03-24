package com.sy.wikitok.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.component.FavItem
import com.sy.wikitok.ui.component.FullScreenImage
import com.sy.wikitok.ui.component.WikiPage
import com.sy.wikitok.ui.screen.MainViewModel.UiState
import com.sy.wikitok.ui.theme.WikiTokTheme
import org.koin.androidx.compose.KoinAndroidContext
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
            KoinAndroidContext {
                WikiTokTheme {

                    val feedViewModel: FeedViewModel = koinViewModel()
                    // val feedUIState by feedViewModel.uiState.collectAsStateWithLifecycle()
                    val feedUIState by feedViewModel.feedUiState.collectAsStateWithLifecycle()

                    val favoriteViewModel: FavoriteViewModel = koinViewModel()
                    val favoriteUIState by favoriteViewModel.favorites.collectAsStateWithLifecycle()

                    LaunchedEffect(key1 = Unit, block = {
                        feedViewModel.loadFeedData()
                    })

                    HomeScaffold(feedUIState, favoriteUIState)
                }
            }
        }
    }
}

@Composable
private fun HomeScaffold(feedUIState: UiState, favoriteUIState: UiState) {
    val feedViewModel: FeedViewModel = koinViewModel()
    val favoriteViewModel: FavoriteViewModel = koinViewModel()

    var currentRoute by rememberSaveable { mutableStateOf(ROUTE_FEED) }

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.height(96.dp)) {
                NavigationBarItem(
                    selected = currentRoute == ROUTE_FEED,
                    onClick = { currentRoute = ROUTE_FEED },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = stringResource(R.string.desc_nav_feed)
                        )
                    }
                )
                NavigationBarItem(
                    selected = currentRoute == ROUTE_FAVORITE,
                    onClick = { currentRoute = ROUTE_FAVORITE },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.desc_nav_favorite),
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
                        onFavoriteToggled = feedViewModel::onFavoriteToggled
                    )
                }

                ROUTE_FAVORITE ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {

                        FavoriteScreen(
                            favoriteUIState,
                            favoriteViewModel::deleteFavorite
                        )
                    }
            }
        }
    }
}


@Composable
private fun FeedScreen(
    feedUiState: UiState,
    onFavoriteToggled: (WikiModel) -> Unit,
    modifier: Modifier = Modifier
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

@Composable
private fun FavoriteScreen(
    favoriteUIState: UiState,
    onItemRemoved: (WikiModel) -> Unit,
) {
    when (favoriteUIState) {
        is UiState.Loading -> LoadingScreen()

        is UiState.Error -> ErrorScreen()

        is UiState.Success -> {
            val favoriteList = favoriteUIState.wikiList
            if (favoriteList.isEmpty()) {
                EmptyScreen()
            } else {
                val fullScreenImageState = rememberSaveable { mutableStateOf(false) }
                val fullScreenItem = rememberSaveable { mutableStateOf<WikiModel?>(null) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(
                        count = favoriteList.size,
                        key = { index -> favoriteList[index].id }
                    ) { index ->
                        FavItem(
                            favoriteList[index],
                            modifier = Modifier.fillMaxWidth(),
                            onDelete = onItemRemoved,
                            onImageTap = { wikiModel ->
                                fullScreenImageState.value = true
                                fullScreenItem.value = wikiModel
                            }
                        )
                    }
                }

                if (fullScreenItem.value != null) {
                    FullScreenImage(
                        imgUrl = fullScreenItem.value!!.imgUrl,
                        visible = fullScreenImageState.value,
                        onClose = {
                            fullScreenImageState.value = false
                        })
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