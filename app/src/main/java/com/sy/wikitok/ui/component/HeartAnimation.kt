package com.sy.wikitok.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sy.wikitok.R
import kotlinx.coroutines.delay

/**
 * @author Yeung
 * @date 2025/4/8
 */
@Composable
fun HeartAnimation(
    triggerAnimation: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
    animationDuration: Int = 400
) {
    var showHeart by remember { mutableStateOf(false) }

    LaunchedEffect(triggerAnimation) {
        if (triggerAnimation) {
            showHeart = true
            delay(animationDuration.toLong())
            showHeart = false
            onAnimationEnd()
        }
    }

    AnimatedVisibility(
        visible = showHeart,
        enter = fadeIn(animationSpec = tween(durationMillis = animationDuration)) +
                scaleIn(
                    animationSpec = tween(durationMillis = animationDuration),
                    initialScale = 0.4f
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = animationDuration)),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = stringResource(R.string.desc_nav_favorite),
            tint = Color.Red,
            modifier = Modifier.size(124.dp)
        )
    }
}
