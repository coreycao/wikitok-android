package com.sy.wikitok.utils

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

/**
 * @author Yeung
 * @date 2025/7/16
 */

fun AssetManager.loadJsonFromAssetsAsFlow(fileName: String): Flow<String?> = flow {
    var jsonString: String? = null
    try {
        // 使用 withContext(Dispatchers.IO) 确保文件读取在 IO 线程进行，避免阻塞主线程
        jsonString = withContext(Dispatchers.IO) {
            this@loadJsonFromAssetsAsFlow.open(fileName).use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                String(buffer, Charsets.UTF_8)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        // 可以根据需要处理异常，例如发射一个错误状态或者一个特定的错误对象
    }
    emit(jsonString) // 发射读取到的 JSON 字符串或 null
}