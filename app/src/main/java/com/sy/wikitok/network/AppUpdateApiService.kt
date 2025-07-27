package com.sy.wikitok.network

import com.sy.wikitok.data.model.AppUpdateInfo
import com.sy.wikitok.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.core.readAvailable
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.use

/**
 * @author Yeung
 * @date 2025/4/2
 */
class AppUpdateApiService(private val httpClient: HttpClient) : BaseApiService {

    companion object {
        private const val ENDPOINT =
            "https://raw.githubusercontent.com/coreycao/wikitok-android/main/backend/app_publish.json"
    }

    /**
     * 请求版本信息
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun observerVersionInfo() = flow {
        emit(
            runCatching {
                val response = httpClient.get(ENDPOINT)
                val responseBody = response.bodyAsText()
                val json = Json {
                    allowTrailingComma = true
                }
                val appUpdateInfo = json.decodeFromString<AppUpdateInfo>(responseBody)
                Logger.i(
                    tag = "AppUpdate",
                    message = "requestVersionInfo success"
                )
                appUpdateInfo
            }.onFailure {
                Logger.e(
                    tag = "AppUpdate",
                    message = "requestVersionInfo error: ${it.message}"
                )
            }
        )
    }

    fun downloadFile(url: String, destinationFile: File): Flow<DownloadState> = flow {
        try {
            val response: HttpResponse = httpClient.get(url)

            if (!response.status.isSuccess()) {
                Logger.e(
                    "app download request failed: ${response.status.value}"
                )
                emit(DownloadState.Error("文件下载失败，状态码: ${response.status.value}"))
                return@flow
            }

            val totalBytes = response.contentLength() ?: 0
            var downloadedBytes = 0L
            val bufferSize = 1024*8 // buffer
            val buffer = ByteArray(bufferSize)

            // 使用 Ktor 的 ByteReadChannel 分块读取数据
            response.bodyAsChannel().readRemaining().use { channel ->
                destinationFile.outputStream().use { output ->
                    while (!channel.exhausted()) {
                        val readBytes = channel.readAvailable(buffer, 0, buffer.size)
                        if (readBytes <= 0) break
                        output.write(buffer, 0, readBytes)
                        downloadedBytes += readBytes

                        // 计算进度并发射
                        if (totalBytes > 0) {
                            val progress = (downloadedBytes.toFloat() / totalBytes)
                            emit(DownloadState.Progress(progress))
                        }
                    }
                }
            }

            // 下载成功，发射成功状态
            Logger.d("app download success")
            emit(DownloadState.Success(destinationFile))

        } catch (e: Exception) {
            // 捕获异常，发射错误状态
            Logger.e(message = "app download error: ${e.message}")
            emit(DownloadState.Error("下载文件时发生错误: ${e.message}"))
        }
    }
}

sealed class DownloadState {
    data class Progress(val progress: Float) : DownloadState()
    data class Success(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}