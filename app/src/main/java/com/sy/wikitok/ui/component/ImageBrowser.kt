package com.sy.wikitok.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/3/24
 */
@Composable
fun ImageBrowser(
    imageBrowserState: ImageBrowserState,
    modifier: Modifier = Modifier,
    contentDescription: String = "A FullScreen Image",
    contentScale: ContentScale = ContentScale.Fit,
) {
    AnimatedVisibility(visible = imageBrowserState.visible, exit = fadeOut(), enter = fadeIn()) {

        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = modifier
                .onSizeChanged { size ->
                    imageBrowserState.onContainerSizeChanged(size)
                }
                .pointerInput(imageBrowserState.imageLoaded) {
                    if (imageBrowserState.imageLoaded) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                coroutineScope.launch {
                                    if (imageBrowserState.isZoomed) {
                                        imageBrowserState.zoomOut()
                                    } else {
                                        imageBrowserState.zoomInAt(tapOffset)
                                    }
                                }
                            },
                            onTap = {
                                if (imageBrowserState.isZoomed.not()) {
                                    imageBrowserState.hide()
                                }
                            },
                        )
                    }
                }
                .pointerInput(imageBrowserState.isZoomed, imageBrowserState.targetScale) {
                    if (imageBrowserState.isZoomed) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                imageBrowserState.onDrag(dragAmount)
                            }
                        }
                    }
                }
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = imageBrowserState.targetScale
                        scaleY = imageBrowserState.targetScale
                        translationX = imageBrowserState.targetOffset.x
                        translationY = imageBrowserState.targetOffset.y
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageBrowserState.imgUrl ?: "")
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                onSuccess = { state ->
                    imageBrowserState.onImageLoaded(
                        state.result.image.width,
                        state.result.image.height
                    )
                }
            )
        }
    }
}