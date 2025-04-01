package com.sy.wikitok.ui.component

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiModel

/**
 * @author Yeung
 * @date 2025/3/21
 */

@Composable
fun DismissFavItem(
    wikiModel: WikiModel,
    modifier: Modifier = Modifier,
    onImageTap: (WikiModel) -> Unit = {},
    onDelete: (WikiModel) -> Unit = {}
) {

    val swipeBoxState = rememberSwipeToDismissBoxState()

    // 监听滑动状态
    LaunchedEffect(swipeBoxState.currentValue) {
        if (swipeBoxState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete(wikiModel)      // 触发删除回调
            swipeBoxState.reset()    // 重置滑动状态
        }
    }

    SwipeToDismissBox(
        state = swipeBoxState,
        modifier = modifier,
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Red, MaterialTheme.shapes.medium),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.desc_nav_delete),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(16.dp)
                        .size(24.dp),
                    tint = Color.White
                )
            }
        },
    ) {
        FavItem(wikiModel, modifier, onImageTap)
    }
}

@Composable
fun FavItem(
    wikiModel: WikiModel,
    modifier: Modifier = Modifier,
    onImageTap: (WikiModel) -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = wikiModel.imgUrl,
                contentDescription = stringResource(R.string.desc_fav_pic),
                modifier = Modifier
                    .size(56.dp)
                    .clickable {
                        onImageTap(wikiModel)
                    },
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            val uri = wikiModel.linkUrl.toUri().run {
                                if (scheme.isNullOrBlank()) {
                                    buildUpon().scheme("https").build()
                                } else {
                                    this
                                }
                            }
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, uri)
                            )
                        }) {
                Text(
                    text = wikiModel.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = wikiModel.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}