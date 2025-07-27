package com.sy.wikitok.ui.component

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * @author Yeung
 * @date 2025/7/26
 */

@Composable
fun ProgressCircle(
    progress: Float,
    modifier: Modifier = Modifier,
    startAngle: Float = -90f,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 5.dp,
    animationDuration: Int = AnimationConstants.DefaultDurationMillis
) {

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(animationDuration),
    )

    Canvas(modifier = modifier) {

        val canvasWidth = size.width
        val canvasHeight = size.height
        val stroke = strokeWidth.toPx()

        // 为避免 stroke 超出边界，需要在每个方向 inset stroke/2
        val diameter = min(canvasWidth, canvasHeight)
        val insetOffset = Offset(stroke / 2, stroke / 2)
        val arcSize = Size(diameter - stroke, diameter - stroke)

        // 绘制背景圆环
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = insetOffset,
            size = arcSize,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // 绘制进度条圆弧
        drawArc(
            color = progressColor,
            startAngle = startAngle,
            sweepAngle = animatedProgress * 360f,
            useCenter = false,
            topLeft = insetOffset,
            size = arcSize,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}
