package com.sy.wikitok.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * @author Yeung
 * @date 2025/4/9
 */
@OptIn(ExperimentalTime::class)
fun currentDateTime(): String {
    // 获取当前的即时时间
    val now = Clock.System.now()
    // 将即时时间转换为本地日期时间
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    // 格式化本地日期时间
    return formatLocalDateTime(localDateTime)
}

fun formatLocalDateTime(dt: LocalDateTime): String {
    return "%04d-%02d-%02d %02d:%02d:%02d".format(
        dt.year, dt.month.ordinal, dt.day,
        dt.hour, dt.minute, dt.second
    )
}