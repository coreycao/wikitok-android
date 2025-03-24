package com.sy.wikitok.utils

import android.util.Log

/**
 * @author Yeung
 * @date 2025/3/23
 */
object Logger {
    private var logLevel: LogLevel = LogLevel.DEBUG
    private var messageFormatter: (String?) -> String = { it -> it ?: "" }
    private const val TAG = "[AppLogger]"

    sealed class LogLevel(val priority: Int) {
        object VERBOSE : LogLevel(0)
        object DEBUG : LogLevel(1)
        object INFO : LogLevel(2)
        object WARN : LogLevel(3)
        object ERROR : LogLevel(4)
    }

    fun setLogLevel(level: LogLevel = LogLevel.DEBUG) {
        logLevel = level
    }

    fun setLogMessageFormatter(messageFormatter: (String?) -> String) {
        this.messageFormatter = messageFormatter
    }

    private fun log(
        level: LogLevel,
        tag: String,
        message: String,
    ) {
        if (level.priority >= logLevel.priority) {
            val formattedMessage = messageFormatter(message)
            when (level) {
                LogLevel.VERBOSE -> Log.v(tag, formattedMessage)
                LogLevel.DEBUG -> Log.d(tag, formattedMessage)
                LogLevel.INFO -> Log.i(tag, formattedMessage)
                LogLevel.WARN -> Log.w(tag, formattedMessage)
                LogLevel.ERROR -> Log.e(tag, formattedMessage)
            }
        }
    }

    fun v(message: String, tag: String = TAG) = log(LogLevel.VERBOSE, tag, message)
    fun d(message: String, tag: String = TAG) = log(LogLevel.DEBUG, tag, message)
    fun i(message: String, tag: String = TAG) = log(LogLevel.INFO, tag, message)
    fun w(message: String, tag: String = TAG) = log(LogLevel.WARN, tag, message)
    fun e(message: String, tag: String = TAG) = log(LogLevel.ERROR, tag, message)
}