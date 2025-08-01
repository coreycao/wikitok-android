package com.sy.wikitok.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.theme.WikiTokTheme
import com.sy.wikitok.utils.Logger
import com.sy.wikitok.utils.SnackbarManager
import kotlinx.coroutines.FlowPreview
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/3/19
 */

@Serializable
sealed class MainRoute {

    @Serializable
    object MainScreen : MainRoute()

    @Serializable
    object Feed : MainRoute()

    @Serializable
    object Favorite : MainRoute()

    @Serializable
    object Setting : MainRoute()

    @Serializable
    data class Chat(
        val id: String,
        val title: String,
        val content: String,
        val imgUrl: String,
        val linkUrl: String
    ) : MainRoute()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WikiTokTheme {
                HomeScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun HomeScaffold() {

    LaunchedEffect(Unit) {
        Logger.d("HomeScaffold Launched")
    }

    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = {
        SnackbarHost(snackbarHostState)
    }) { innerPadding ->
        LaunchedEffect(key1 = snackbarHostState) {
            SnackbarManager.snackbarFlow.collect { snackbarData ->
                if (snackbarData != null) {
                    val result = snackbarHostState.showSnackbar(
                        message = snackbarData.message,
                        actionLabel = snackbarData.actionLabel,
                        duration = snackbarData.duration
                    )
                    when (result) {
                        androidx.compose.material3.SnackbarResult.ActionPerformed -> {
                            snackbarData.onAction?.invoke()
                        }

                        androidx.compose.material3.SnackbarResult.Dismissed -> {
                            // Snackbar closed
                        }
                    }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = MainRoute.MainScreen,
            modifier = Modifier.consumeWindowInsets(innerPadding)
        ) {

            // Home Screen with bottom bar
            composable<MainRoute.MainScreen> {
                MainScreenWithBottomBar(
                    homeInnerPadding = innerPadding,
                    navController = navController
                )
            }

            // AI Chat Screen
            composable<MainRoute.Chat>(
            ) { it ->
                val route = it.toRoute<MainRoute.Chat>()
                val wikiModel = WikiModel(
                    id = route.id,
                    title = route.title,
                    content = route.content,
                    imgUrl = route.imgUrl,
                    linkUrl = route.linkUrl
                )
                ChatScreen(
                    modifier = Modifier.padding(innerPadding),
                    wikiInfo = wikiModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreenWithBottomBar(homeInnerPadding: PaddingValues, navController: NavHostController) {
    val mainViewModel = koinViewModel<MainViewModel>()
    val internalNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(84.dp)
            ) {
                val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                NavBarItem.items.forEach { navBarItem ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(
                                navBarItem.route::class
                            )
                        } == true,
                        onClick = {
                            internalNavController.navigate(navBarItem.route) {
                                popUpTo(internalNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = navBarItem.icon,
                        label = {
                            Text(stringResource(navBarItem.label))
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = internalNavController,
            startDestination = MainRoute.Feed,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable<MainRoute.Feed> {
                FeedScreen(
                    // modifier = Modifier.padding(innerPadding),
                    navigateToChat = { wikiModel ->
                    navController.navigate(MainRoute.Chat(wikiModel.id, wikiModel.title,wikiModel.content, wikiModel.imgUrl, wikiModel.linkUrl))
                })
            }

            composable<MainRoute.Favorite> {
                FavoriteScreen(
                    modifier = Modifier.padding(top = homeInnerPadding.calculateTopPadding())
                )
            }

            composable<MainRoute.Setting> {
                SettingScreen(
                    modifier = Modifier.padding(top = homeInnerPadding.calculateTopPadding())
                )
            }
        }
    }
}

// bottom navbar item
private class NavBarItem(
    val route: MainRoute,
    val label: Int,
    val icon: @Composable () -> Unit = {}
) {
    companion object {
        val items = listOf(
            NavBarItem(MainRoute.Feed, R.string.desc_nav_feed) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.desc_nav_feed)
                )
            },
            NavBarItem(MainRoute.Favorite, R.string.desc_nav_favorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.desc_nav_favorite),
                )
            },
            NavBarItem(MainRoute.Setting, R.string.desc_nav_setting) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.desc_nav_setting),
                )
            }
        )
    }
}