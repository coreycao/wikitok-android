package com.sy.wikitok.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sy.wikitok.R
import com.sy.wikitok.ui.theme.WikiTokTheme
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/3/19
 */

// Route
private const val ROUTE_FEED = "feed"
private const val ROUTE_FAVORITE = "favorite"
private const val ROUTE_SETTING = "setting"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KoinAndroidContext {
                WikiTokTheme {
                    HomeScaffold()
                }
            }
        }
    }
}

private class NavBarItem(
    val screenRoute: String,
    val icon: @Composable () -> Unit = {},
) {
    companion object {
        val items = listOf(
            NavBarItem(ROUTE_FEED) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.desc_nav_feed)
                )
            },
            NavBarItem(ROUTE_FAVORITE) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.desc_nav_favorite),
                )
            },
            NavBarItem(ROUTE_SETTING) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.desc_nav_setting),
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun HomeScaffold() {

    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    val mainViewModel = koinViewModel<MainViewModel>()

    val feedViewModel = koinViewModel<FeedViewModel>()

    LaunchedEffect(Unit) {
        Logger.d(tag = "MainActivity", message = "observerSnakeBarEvent")

        mainViewModel.snakebarEvent
            .debounce(300)
            .collect {
                snackbarHostState.showSnackbar(it.message)
            }

    }

    LaunchedEffect(Unit) {
        Logger.d(tag = "MainActivity", message = "initFeedData")
        feedViewModel.initFeedData()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(96.dp)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                NavBarItem.items.forEach { navBarItem ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == navBarItem.screenRoute } == true,
                        onClick = {
                            navController.navigate(navBarItem.screenRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            // currentRoute = navBarItem.screenRoute
                        },
                        icon = navBarItem.icon
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_FEED,
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            composable(ROUTE_FEED) {
                FeedScreen(feedViewModel)
            }

            composable(ROUTE_FAVORITE) {
                Surface(
                    modifier = Modifier
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    FavoriteScreen()
                }
            }

            composable(ROUTE_SETTING) {
                Surface(
                    modifier = Modifier
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    SettingScreen(mainViewModel)
                }
            }
        }
    }
}
