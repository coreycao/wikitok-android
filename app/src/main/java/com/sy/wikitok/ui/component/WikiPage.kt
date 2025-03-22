package com.sy.wikitok.ui.component

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sy.wikitok.ui.theme.WikiTokTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import com.sy.wikitok.R

/**
 * @author Yeung
 * @date 2025/3/20
 */

@Composable
fun WikiPage(
    title: String,
    content: String,
    imgUrl: String,
    linkUrl: String,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        NetworkImage(
            modifier = Modifier.fillMaxSize(),
            url = imgUrl,
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
                        text = title,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    )
                    if (isFavorite){
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.desc_nav_favorite),
                            tint = Color.Red
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
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
                            val uri = linkUrl.toUri().run {
                                if (scheme.isNullOrBlank()) {
                                    buildUpon().scheme("https").build()
                                } else {
                                    this
                                }
                            }
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, uri)
                            )
                        }
                )
            }
        }
    }
}

@Preview(name = "preview_wikipage", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewWikiPage() {
    WikiTokTheme {
        WikiPage(
            "标题",
            "内容".repeat(20),
            "",
            linkUrl = "https://zh.wikipedia.org/wiki/示例页面",
            isFavorite = true
        )
    }
}