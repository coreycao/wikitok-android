package com.sy.wikitok.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.component.FavItem
import com.sy.wikitok.utils.Logger

/**
 * @author Yeung
 * @date 2025/3/24
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    searchUiState: MainViewModel.UiState,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var expanded by rememberSaveable { mutableStateOf(false) }

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
                    onQueryChange = onSearchQueryChanged,
                    onSearch = {
                        Logger.d(
                            tag = "SearchBar#inputField#onSearch",
                            message = "expanded: $expanded"
                        )
                        expanded = false
                        keyboardController?.hide()
                    },
                    expanded = expanded,
                    onExpandedChange = {
                        Logger.d(tag = "SearchBar#inputField", message = "expanded: $expanded")
                        expanded = it
                    },
                    placeholder = { Text(stringResource(R.string.txt_search_bar_hint)) },
                    leadingIcon = {
                        if (expanded) {
                            IconButton(
                                onClick = {
                                    // onSearchQueryChanged("")
                                    keyboardController?.hide()
                                    expanded = false
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
                                onSearchQueryChanged("")
                                keyboardController?.hide()
                                // expanded = false
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
            expanded = expanded,
            onExpandedChange = {
                Logger.d(tag = "SearchBar#searchbar", message = "expanded: $expanded")
                expanded = it
            },
        ) {
            when (searchUiState) {
                is MainViewModel.UiState.Success -> {
                    if (searchUiState.wikiList.isEmpty()) {
                        EmptyScreen(
                            modifier = Modifier.clickable(
                                onClick = {
                                    Logger.d(
                                        tag = "SearchResultContent#EmptyScreen",
                                        message = "onClick"
                                    )
                                    onSearchQueryChanged("")
                                    expanded = false
                                }
                            ))
                    } else {
                        SearchResultContent(
                            favoriteList = searchUiState.wikiList
                        )
                    }
                }

                is MainViewModel.UiState.Error -> {
                    ErrorScreen(
                        modifier = Modifier.clickable(
                            onClick = {
                                Logger.d(
                                    tag = "SearchResultContent#ErrorScreen",
                                    message = "onClick"
                                )
                                onSearchQueryChanged("")
                                expanded = false
                            }
                        ))
                }

                is MainViewModel.UiState.Loading -> {
                    LoadingScreen()
                }
            }
        }
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