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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sy.wikitok.R
import com.sy.wikitok.ui.theme.WikiTokTheme
import com.sy.wikitok.utils.SnackbarManager
import kotlinx.coroutines.FlowPreview
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.KoinAndroidContext

/**
 * @author Yeung
 * @date 2025/3/19
 */

@Serializable
sealed class MainRoute {
    @Serializable
    object Feed : MainRoute()

    @Serializable
    object Favorite : MainRoute()

    @Serializable
    object Setting : MainRoute()
}

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

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun HomeScaffold() {

    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

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
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(navBarItem.route::class) } == true,
                        onClick = {
                            navController.navigate(navBarItem.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
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
            startDestination = MainRoute.Feed,
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            composable<MainRoute.Feed> {
                FeedScreen()
            }

            composable<MainRoute.Favorite> {
                FavoriteScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
            }

            composable<MainRoute.Setting> {
                SettingScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
            }
        }
    }
}

// bottom navbar item
private class NavBarItem(
    val route: MainRoute,
    val icon: @Composable () -> Unit = {},
) {
    companion object {
        val items = listOf(
            NavBarItem(MainRoute.Feed) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.desc_nav_feed)
                )
            },
            NavBarItem(MainRoute.Favorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.desc_nav_favorite),
                )
            },
            NavBarItem(MainRoute.Setting) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.desc_nav_setting),
                )
            }
        )
    }
}