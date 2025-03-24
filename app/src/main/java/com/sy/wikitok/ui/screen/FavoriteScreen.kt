package com.sy.wikitok.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.component.DismissFavItem
import com.sy.wikitok.ui.component.FullScreenImage
import com.sy.wikitok.ui.screen.MainViewModel.UiState

/**
 * @author Yeung
 * @date 2025/3/24
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteUIState: UiState,
    onItemRemoved: (WikiModel) -> Unit,
    searchQuery: String,
    searchResultUIState: UiState,
    onSearchQueryChanged: (String) -> Unit
) {
    when (favoriteUIState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen()
        is UiState.Success -> {
            val favoriteList = favoriteUIState.wikiList
            if (favoriteList.isEmpty()) {
                EmptyScreen()
            } else {
                FavoriteListScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = SearchBarDefaults.InputFieldHeight),
                    favoriteList = favoriteList,
                    onItemRemoved = onItemRemoved
                )
                SearchScreen(
                    modifier = Modifier.fillMaxSize(),
                    searchUiState = searchResultUIState,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = onSearchQueryChanged
                )

            }
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
