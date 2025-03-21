package com.sy.wikitok.ui.component

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sy.wikitok.R
import com.sy.wikitok.data.model.WikiArticle

/**
 * @author Yeung
 * @date 2025/3/21
 */

@Composable
fun FavItem(wikiArticle: WikiArticle, modifier: Modifier) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(96.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            NetworkImage(
                url = wikiArticle.imgUrl,
                contentDescription = stringResource(R.string.desc_fav_pic),
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop,
            )


            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = wikiArticle.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = wikiArticle.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = stringResource(R.string.txt_read_more),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val uri = wikiArticle.linkUrl.toUri().run {
                                if (scheme.isNullOrBlank()) {
                                    buildUpon().scheme("https").build()
                                } else {
                                    this
                                }
                            }
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, uri)
                            )
                        },
                    textAlign = TextAlign.End
                )
            }
        }
    }
}