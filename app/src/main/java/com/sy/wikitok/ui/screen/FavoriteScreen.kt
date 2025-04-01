package com.sy.wikitok.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sy.wikitok.ui.component.FullScreenImage
import com.sy.wikitok.ui.screen.FavoriteViewModel.UiState
import com.sy.wikitok.utils.Logger
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/3/24
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen() {

    val favoriteViewModel = koinViewModel<FavoriteViewModel>()

    val favoriteUIState by favoriteViewModel.favorites.collectAsStateWithLifecycle()

    when (favoriteUIState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen()
        is UiState.Empty -> EmptyScreen()
        is UiState.Success -> {
            val favoriteList = (favoriteUIState as UiState.Success).wikiList
            SearchScreen(
                modifier = Modifier.fillMaxSize(),
                favoriteList = favoriteList
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteListScreen(
    modifier: Modifier = Modifier,
    favoriteList: List<WikiModel>,
    onItemRemoved: (WikiModel) -> Unit
) {
    val fullScreenImageState = rememberSaveable { mutableStateOf(false) }
    val fullScreenItem = rememberSaveable { mutableStateOf<WikiModel?>(null) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            count = favoriteList.size,
            key = { index -> favoriteList[index].id }
        ) { index ->
            DismissFavItem(
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
            onItemRemoved = favoriteViewModel::deleteFavorite
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
        items(
            count = favoriteList.size,
            key = { index -> favoriteList[index].id }
        ) { index ->
            FavItem(
                wikiModel = favoriteList[index],
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
