package com.sy.wikitok.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

/**
 * @author Yeung
 * @date 2025/3/24
 */
@Composable
fun FullScreenImage(imgUrl: String, visible: Boolean, onClose: () -> Unit) {
    AnimatedVisibility(visible = visible, exit = fadeOut(), enter = fadeIn()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable {
                    onClose()
                }) {
            NetworkImage(
                url = imgUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onClose()
                    },
                contentDescription = "A FullScreen Image",
                contentScale = ContentScale.Fit
            )
        }
    }
}