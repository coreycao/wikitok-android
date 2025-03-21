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
import com.sy.wikitok.data.model.WikiArticle
import com.sy.wikitok.ui.component.FavItem
import com.sy.wikitok.ui.component.IconFavorite
import com.sy.wikitok.ui.component.IconHome
import com.sy.wikitok.ui.component.WikiPage
import com.sy.wikitok.ui.theme.WikiTokTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

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
                val viewModel: MainViewModel by viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = Unit, block = {
                    viewModel.loadWikiList()
                    viewModel.loadFavoriteList()
                })

                HomeScaffold(uiState)
            }
        }
    }
}

@Composable
private fun HomeScaffold(uiState: MainViewModel.MainUiState) {
    val viewModel: MainViewModel = koinViewModel()
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
                    when (uiState) {
                        is MainViewModel.MainUiState.Loading -> LoadingScreen()
                        is MainViewModel.MainUiState.Success -> FeedScreen(
                            uiState.wikiList,
                            viewModel::onDoubleTab
                        )

                        is MainViewModel.MainUiState.Error -> ErrorScreen(uiState.message)
                    }
                }

                ROUTE_FAVORITE ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        FavoriteScreen()
                    }
            }
        }
    }
}

@Composable
private fun FeedScreen(wikiList: List<WikiArticle>, onDoubleTab: (WikiArticle) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { wikiList.size })

    if (wikiList.isEmpty()) {
        LoadingScreen()
        return
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize(),
    ) { page ->
        val article = wikiList[page]

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onDoubleTab(article)
                        })
                }) {
            WikiPage(
                title = article.title,
                content = article.content,
                imgUrl = article.imgUrl,
                articleUrl = article.linkUrl,
                isFavorite = article.isFavorite,
            )

        }
    }
}

@Composable
private fun FavoriteScreen(
    viewModel: MainViewModel = koinViewModel(),
) {
    val favoriteList by viewModel.favoriteListState.collectAsStateWithLifecycle()
    if (favoriteList.isEmpty()) {
        EmptyScreen()
        return
    }

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
private fun ErrorScreen(errorMessage: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.txt_feed_error))
    }
}