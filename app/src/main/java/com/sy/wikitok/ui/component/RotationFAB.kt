package com.sy.wikitok.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

/**
 * @author Yeung
 * @date 2025/7/24
 */
@Composable
fun RotationFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotated by remember { mutableStateOf(false) }
    var rotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(targetValue = rotation, label = "")

    FloatingActionButton(
        onClick = {
            onClick()
            rotated = !rotated
            if (rotated) rotation += 90f else rotation -= 90
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (rotated) Icons.Filled.Close else Icons.Filled.Face,
            contentDescription = null,
            modifier = Modifier.rotate(animatedRotation)
        )
    }
}