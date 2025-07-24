package com.sy.wikitok.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sy.wikitok.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/4/8
 */
@Composable
fun HeartAnimation(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    onAnimationEnd: () -> Unit = {},
    animationDuration: Int = 400,
    content: @Composable BoxScope.() -> Unit
) {
    var showHeart by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var animJob: Job? = null

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onDoubleTap = {
                if (animJob?.isActive == true) {
                    return@detectTapGestures
                }
                animJob = scope.launch {
                    showHeart = true
                    delay(animationDuration.toLong())
                    showHeart = false
                    onAnimationEnd()
                }
            })
        },
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        content()
        AnimatedVisibility(
            visible = showHeart,
            enter = fadeIn(animationSpec = tween(durationMillis = animationDuration)) +
                    scaleIn(
                        animationSpec = tween(durationMillis = animationDuration),
                        initialScale = 0.4f
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = animationDuration)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(R.string.desc_nav_favorite),
                tint = Color.Red,
                modifier = Modifier.size(124.dp)
            )
        }
    }
}
