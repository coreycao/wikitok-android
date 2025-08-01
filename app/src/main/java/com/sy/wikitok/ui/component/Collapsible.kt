package com.sy.wikitok.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author Yeung
 * @date 2025/7/31
 *
 * 通用的可折叠/展开容器组件。
 *
 * @param modifier 修饰符。
 * @param collapsedHeight 折叠状态下的最大高度。
 * @param initiallyExpanded 是否初始为展开状态。
 * @param content 需要被折叠/展开的 Composable 内容。
 */
@Composable
fun Collapsible(
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 100.dp,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    // 展开/折叠状态
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    // 存储内容的完整渲染高度（像素）
    var fullContentHeight by remember { mutableStateOf<Int?>(null) }

    // 将 Dp 转换为 Px，用于后续比较
    val collapsedHeightPx = with(LocalDensity.current) { collapsedHeight.toPx() }

    // 判断是否需要折叠功能：当内容的完整高度大于折叠高度时
    val hasOverflow by remember(fullContentHeight) {
        derivedStateOf {
            fullContentHeight != null && fullContentHeight!! > collapsedHeightPx
        }
    }

    Column(
        modifier = modifier
            // 核心动画：当Column尺寸变化时，平滑过渡
            .animateContentSize()
    ) {
        // 包裹内容的容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // 关键修饰符：限制最大高度。展开时无限制，折叠时限制为 collapsedHeight
                .heightIn(max = if (isExpanded) Dp.Unspecified else collapsedHeight)
                // 确保内容不会绘制到限制区域之外
                .clipToBounds()
        ) {
            // 这个 Box 用于测量内容的真实高度
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        // 首次测量时，记录内容的完整高度
                        if (fullContentHeight == null) {
                            fullContentHeight = size.height
                        }
                    }
            ) {
                // 调用者传入的实际内容
                content()
            }

            // 添加渐变蒙层，当未展开且内容溢出时显示
            if (!isExpanded && hasOverflow) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(collapsedHeight / 2) // 蒙层高度为折叠高度的一半
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                            )
                        )
                )
            }
        }

        // 仅当内容溢出时，才显示“展开/收起”的控制区域
        if (hasOverflow) {
            ControlRow(
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded }
            )
        }
    }
}

/**
 * “展开/收起”的控制行，包含文字和可旋转的箭头
 */
@Composable
private fun ControlRow(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isExpanded) "收起" else "展开",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            label = "arrowRotation"
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "收起" else "展开",
            modifier = Modifier
                .padding(start = 4.dp)
                .rotate(rotationAngle)
        )
    }
}