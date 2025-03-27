package com.sy.wikitok.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sy.wikitok.R
import com.sy.wikitok.ui.theme.WikiTokTheme
import org.koin.androidx.compose.KoinAndroidContext

/**
 * @author Yeung
 * @date 2025/3/19
 */

// Route
private const val ROUTE_FEED = "feed"
private const val ROUTE_FAVORITE = "favorite"

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

@Composable
private fun HomeScaffold() {

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
                    FeedScreen()
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
