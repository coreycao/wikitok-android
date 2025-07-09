package com.sy.wikitok.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/7/9
 */

@Composable
fun rememberImageBrowserState(): ImageBrowserState {
    return remember {
        ImageBrowserState()
    }
}

class ImageBrowserState internal constructor(
    private val initialScale: Float = 1f,
    private val maxScale: Float = 3f,
) {
    var visible by mutableStateOf(false)
        private set

    var imgUrl by mutableStateOf<String?>(null)
        private set

    var imageLoaded by mutableStateOf(false)
        private set

    var imageSize by mutableStateOf(IntSize.Zero)
        private set

    var containerSize by mutableStateOf(IntSize.Zero)
        private set

    val isZoomed: Boolean
        get() = scaleAnim.value > initialScale + 0.01f

    val targetScale: Float get() = scaleAnim.value

    val targetOffset: Offset get() = offsetAnim.value

    private val scaleAnim = Animatable(initialScale)

    private val offsetAnim = Animatable(Offset.Zero, Offset.VectorConverter)

    fun show(imgUrl: String) {
        this.imgUrl = imgUrl
        this.visible = true
    }

    fun hide() {
        imgUrl = null
        visible = false
        imageLoaded = false
        imageSize = IntSize.Zero
        containerSize = IntSize.Zero
    }

    internal fun onImageLoaded(width: Int, height: Int) {
        imageLoaded = true
        imageSize = IntSize(width, height)
    }

    internal fun onContainerSizeChanged(size: IntSize) {
        containerSize = size
    }

    internal suspend fun zoomInAt(tapOffset: Offset) {
        val targetScale = maxScale
        val rawOffset = Offset(
            x = -(tapOffset.x - containerSize.width / 2f) * targetScale,
            y = -(tapOffset.y - containerSize.height / 2f) * targetScale
        )
        val maxOffset =
            calculateMaxOffset(containerSize, imageSize, targetScale)
        val boundedOffset =
            rawOffset.coerceInBounds(maxOffset.x, maxOffset.y)
        coroutineScope {
            launch {
                scaleAnim.animateTo(
                    targetScale,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            }
            launch {
                offsetAnim.animateTo(
                    boundedOffset,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    internal suspend fun zoomOut() {
        coroutineScope {
            launch {
                scaleAnim.animateTo(initialScale, tween(300))
            }
            launch {
                offsetAnim.animateTo(Offset.Zero, tween(300))
            }
        }
    }

    internal suspend fun onDrag(dragAmount: Offset) {
        val maxOffset =
            calculateMaxOffset(containerSize, imageSize, targetScale)

        val rawOffset = targetOffset + dragAmount
        val boundedOffset = rawOffset.coerceInBounds(maxOffset.x, maxOffset.y)
        offsetAnim.snapTo(boundedOffset)
    }

}

fun Offset.coerceInBounds(maxOffsetX: Float, maxOffsetY: Float): Offset {
    return Offset(
        x = x.coerceIn(-maxOffsetX, maxOffsetX),
        y = y.coerceIn(-maxOffsetY, maxOffsetY)
    )
}

fun calculateMaxOffset(
    containerSize: IntSize,
    realImageSize: IntSize,
    scale: Float
): Offset {
    val containerWidth = containerSize.width.toFloat()
    val containerHeight = containerSize.height.toFloat()

    val realImgWidth = realImageSize.width.toFloat()
    val realImgHeight = realImageSize.height.toFloat()

    var imageWidth = 0f
    var imageHeight = 0f

    // 图片是 Fit 在容器中的，此处根据宽高比计算出图片在屏幕上真实显示的尺寸
    if (containerWidth / containerHeight <= realImgWidth / realImgHeight) {
        imageWidth = containerWidth
        imageHeight = realImgHeight / realImgWidth * imageWidth
    } else {
        imageHeight = containerHeight
        imageWidth = realImgWidth / realImgHeight * imageHeight
    }

    val maxOffsetX =
        ((imageWidth * scale - containerWidth) / 2).coerceAtLeast(
            0f
        )
    val maxOffsetY =
        ((imageHeight * scale - containerHeight) / 2).coerceAtLeast(
            0f
        )
    return Offset(maxOffsetX, maxOffsetY)
}