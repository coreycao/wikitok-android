package com.sy.wikitok.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.component.DismissFavItem
import com.sy.wikitok.ui.component.FavItem
import com.sy.wikitok.ui.component.ImageBrowser
import com.sy.wikitok.ui.component.MarkdownPreview
import com.sy.wikitok.ui.component.RotationFAB
import com.sy.wikitok.ui.component.rememberImageBrowserState
import com.sy.wikitok.ui.screen.FavoriteViewModel.UiState
import com.sy.wikitok.utils.Logger
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/3/24
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
    favoriteViewModel: FavoriteViewModel = koinViewModel()
) {

    LaunchedEffect(Unit) {
        Logger.d("FavoriteScreen LaunchedEffect")
    }

    val favoriteUIState by favoriteViewModel.favorites.collectAsStateWithLifecycle()

    when (favoriteUIState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen()
        is UiState.Empty -> EmptyScreen()
        is UiState.Success -> {
            val favoriteList = (favoriteUIState as UiState.Success).wikiList
            /*SearchScreen(
                modifier = Modifier.fillMaxSize(),
                favoriteList = favoriteList
            )*/
            FavoriteListScreen(
                modifier = modifier.fillMaxSize(),
                favoriteList = favoriteList,
                onItemRemoved = favoriteViewModel::deleteFavorite,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteListScreen(
    modifier: Modifier = Modifier,
    favoriteList: List<WikiModel>,
    onItemRemoved: (WikiModel) -> Unit,
    onItemImageTap: (WikiModel) -> Unit = { _ -> }
) {
    val imageBrowserState = rememberImageBrowserState()

    val viewModel = koinViewModel<FavoriteViewModel>()

    val aiSummaryState by viewModel.aiSummaryState.collectAsStateWithLifecycle()

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            val itemModifier = Modifier.fillMaxWidth()
            items(
                count = favoriteList.size,
                key = { index -> favoriteList[index].id }
            ) { index ->
                DismissFavItem(
                    favoriteList[index],
                    modifier = itemModifier,
                    onImageTap = { wikiModel ->
                        imageBrowserState.show(wikiModel.imgUrl)
                        onItemImageTap(wikiModel)
                    },
                    onDelete = onItemRemoved
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState() )
            ) {
                when (aiSummaryState) {
                    is FavoriteViewModel.AISummaryState.Loading -> {
                        LoadingScreen()
                    }

                    is FavoriteViewModel.AISummaryState.Empty->{
                        EmptyScreen()
                    }

                    is FavoriteViewModel.AISummaryState.Error -> {
                        ErrorScreen()
                    }

                    is FavoriteViewModel.AISummaryState.Success -> {
                        val summaryContent = (aiSummaryState as FavoriteViewModel.AISummaryState.Success).summary
                        MarkdownPreview(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = summaryContent
                        )
                    }
                }
            }
        }

        RotationFAB(
            onClick = {
                expanded = !expanded
                if (expanded){
                    viewModel.aiSummary()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    ImageBrowser(
        imageBrowserState,
        modifier = Modifier.background(Color.Black.copy(alpha = 0.9f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    favoriteList: List<WikiModel>
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val favoriteViewModel = koinViewModel<FavoriteViewModel>()

    val searchUiState by favoriteViewModel.searchResult.collectAsStateWithLifecycle()
    val searchQuery by favoriteViewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by favoriteViewModel.isSearching.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = favoriteViewModel::onSearchQueryChanged,
                    onSearch = {
                        keyboardController?.hide()
                    },
                    expanded = isSearching,
                    onExpandedChange = {
                        Logger.d(tag = "SearchBar#inputField", message = "expanded: $it")
                        favoriteViewModel.toggleSearchBar(it)
                    },
                    placeholder = { Text(stringResource(R.string.txt_search_bar_hint)) },
                    leadingIcon = {
                        if (isSearching) {
                            IconButton(
                                onClick = {
                                    keyboardController?.hide()
                                    favoriteViewModel.toggleSearchBar(false)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Search,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = stringResource(R.string.desc_search_bar)
                            )
                        }

                    }, trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                keyboardController?.hide()
                                favoriteViewModel.onSearchQueryChanged("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = "Clear search query"
                                )
                            }
                        }
                    }
                )
            },
            expanded = isSearching,
            onExpandedChange = {
                Logger.d(tag = "SearchBar#searchbar", message = "expanded: $it")
                favoriteViewModel.toggleSearchBar(it)
            },
        ) {
            when (searchUiState) {
                is UiState.Empty -> {
                    EmptyScreen(
                        modifier = Modifier.clickable(
                            onClick = {
                                Logger.d(
                                    tag = "SearchResultContent#EmptyScreen",
                                    message = "onClick"
                                )
                                favoriteViewModel.toggleSearchBar(false)
                            }
                        ))
                }

                is UiState.Success -> {
                    val searchResultList = (searchUiState as UiState.Success).wikiList
                    SearchResultContent(
                        favoriteList = searchResultList
                    )
                }

                is UiState.Error -> {
                    ErrorScreen(
                        modifier = Modifier.clickable(
                            onClick = {
                                Logger.d(
                                    tag = "SearchResultContent#ErrorScreen",
                                    message = "onClick"
                                )
                                favoriteViewModel.toggleSearchBar(false)
                            }
                        ))
                }

                is UiState.Loading -> {
                    LoadingScreen()
                }
            }
        }

        FavoriteListScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = SearchBarDefaults.InputFieldHeight)
                .semantics { traversalIndex = 1f },
            favoriteList = favoriteList,
            onItemRemoved = favoriteViewModel::deleteFavorite,
        )

    }
}

@Composable
fun SearchResultContent(
    modifier: Modifier = Modifier,
    favoriteList: List<WikiModel>
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        val itemModifier = Modifier.fillMaxWidth()
        items(
            count = favoriteList.size,
            key = { index -> favoriteList[index].id }
        ) { index ->
            FavItem(
                wikiModel = favoriteList[index],
                modifier = itemModifier,
            )
        }
    }
}
