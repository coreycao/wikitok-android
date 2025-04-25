package com.sy.wikitok.utils

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/4/19
 */

data class SnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val onAction: (() -> Unit)? = null
)

object SnackbarManager {
    private val _snackbarFlow = MutableSharedFlow<SnackbarData?>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    private var currentSnackbarJob: Job? = null
    private const val DEFAULT_DURATION_MS = 3000L // 默认显示时长 3 秒

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null
    ) {
        currentSnackbarJob?.cancel() // 取消正在显示的 Snackbar

        currentSnackbarJob = CoroutineScope(Dispatchers.Main).launch {
            _snackbarFlow.emit(
                SnackbarData(
                    message = message,
                    actionLabel = actionLabel,
                    duration = duration,
                    onAction = onAction
                )
            )
            // 如果 duration 是 Indefinite，则不自动取消，否则在指定时间后发送 null 关闭 Snackbar
            if (duration != SnackbarDuration.Indefinite) {
                kotlinx.coroutines.delay(
                    when (duration) {
                        SnackbarDuration.Short -> 1500L // Short 的默认时长
                        SnackbarDuration.Long -> 2750L  // Long 的默认时长
                        else -> DEFAULT_DURATION_MS
                    }
                )
                _snackbarFlow.emit(null) // 发送 null 以关闭 Snackbar
            }
        }
    }

    // 用于在不需要 Action 的情况下显示 Snackbar
    fun showSnackbar(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        showSnackbar(message = message, actionLabel = null, duration = duration, onAction = null)
    }

    // 用于立即关闭当前显示的 Snackbar
    fun dismissSnackbar() {
        currentSnackbarJob?.cancel()
        CoroutineScope(Dispatchers.Main).launch {
            _snackbarFlow.emit(null)
        }
    }
}