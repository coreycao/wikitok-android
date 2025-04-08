package com.sy.wikitok.data.model

import kotlinx.serialization.Serializable

/**
 * @author Yeung
 * @date 2025/4/6
 */

@Serializable
data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val forceUpdate: Boolean,
    val minSupportVersion: String,
    val latestVersion: String,
    val releaseNotes: List<ReleaseNote>,
    val downloadUrl: DownloadUrl
)

@Serializable
data class ReleaseNote(
    val version: String,
    val date: String,
    val notes: List<String>
)

@Serializable
data class DownloadUrl(
    val android: PlatformDownloadInfo,
    val ios: PlatformDownloadInfo
)

@Serializable
data class PlatformDownloadInfo(
    val url: String,
    val md5: String
)