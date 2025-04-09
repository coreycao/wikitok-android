package com.sy.wikitok.utils

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

/**
 * @author Yeung
 * @date 2025/4/9
 */
fun currentDateTime(): String {
    return LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    )
}