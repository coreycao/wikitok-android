package com.sy.wikitok.ui.component

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sy.wikitok.R
import com.sy.wikitok.ui.theme.WikiTokTheme

/**
 * @author Yeung
 * @date 2025/3/21
 */

@Composable
fun FavItem() {
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
            Image(
                imageVector = IconFavorite,
                contentDescription = stringResource(R.string.desc_fav_pic),
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterStart
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "收藏标题",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = "收藏内容".repeat(20),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = "阅读更多 >>",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewFavItem() {
    WikiTokTheme {
        FavItem()
    }
}