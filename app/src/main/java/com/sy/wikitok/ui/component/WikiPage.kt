package com.sy.wikitok.ui.component

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiModel

/**
 * @author Yeung
 * @date 2025/3/20
 */

@Composable
fun WikiPage(
    wikiModel: WikiModel,
    onDoubleTapped: (WikiModel) -> Unit = {},
    onFavIconTapped: (WikiModel) -> Unit = {},
    onDetailClicked:()->Unit={}
) {
    val context = LocalContext.current

    // get status bar's height
    val view = LocalView.current
    val windowInsets = ViewCompat.getRootWindowInsets(view)
    val statusBarHeight = windowInsets?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeight.toDp() }

    HeartAnimation(modifier = Modifier.fillMaxSize(), onAnimationEnd = { onDoubleTapped(wikiModel) }) {
        NetworkImage(
            modifier = Modifier.fillMaxSize(),
            url = wikiModel.imgUrl,
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(R.string.desc_feed_bg),
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
            shape = MaterialTheme.shapes.small,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = wikiModel.title,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        modifier = Modifier.weight(1f),
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.desc_nav_favorite),
                        tint = if (wikiModel.isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier
                            .clickable {
                                onFavIconTapped(wikiModel)
                            }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = wikiModel.content,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.txt_read_more),
                    color = Color.White,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDetailClicked()
                            /*val uri = wikiModel.linkUrl.toUri().run {
                                if (scheme.isNullOrBlank()) {
                                    buildUpon().scheme("https").build()
                                } else {
                                    this
                                }
                            }
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, uri)
                            )*/
                        }
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(R.string.desc_nav_share),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusBarHeightDp, end = 32.dp)
                .size(28.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${wikiModel.title}: ${wikiModel.linkUrl}")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share"))
                }
        )
    }
}

