package com.sy.wikitok.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.BuildConfig
import com.sy.wikitok.R
import com.sy.wikitok.data.Langs
import com.sy.wikitok.data.Language
import com.sy.wikitok.ui.component.NetworkImage
import com.sy.wikitok.ui.component.ProgressCircle
import com.sy.wikitok.utils.Logger
import com.sy.wikitok.utils.SnackbarManager
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

/**
 * @author Yeung
 * @date 2025/3/27
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        Logger.d("SettingScreen:LaunchedEffect")
    }

    val context = LocalContext.current

    // 处理 SideEffect
    LaunchedEffect(settingViewModel.effect) {
        settingViewModel.effect.collect { effect ->
            when (effect) {
                is SettingViewModel.Effect.Toast -> {
                    SnackbarManager.showSnackbar(effect.message)
                }

                is SettingViewModel.Effect.Export -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.exportedData)
                    }
                    context.startActivity(intent)
                }

                is SettingViewModel.Effect.Update -> {
                    SnackbarManager.showSnackbar(
                        "发现新版本", actionLabel = "下载",
                        onAction = {
                            val downloadPath = context.getExternalFilesDir(DIRECTORY_DOWNLOADS)
                            settingViewModel.downloadAppUpdate(effect.versionInfo, downloadPath)
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        }) { innerPadding ->

        val dialogState = settingViewModel.dialogState.collectAsStateWithLifecycle()

        val downloadState = settingViewModel.downloadState.collectAsStateWithLifecycle()

        fun installApk() {
            downloadState.value.let { state ->
                if (state is SettingViewModel.DownloadUiState.Completed) {
                    // 使用 FileProvider 获取文件的 Uri
                    val apkUri: Uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        state.file
                    )

                    // 创建安装 Intent
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(installIntent)
                }
            }

        }

        val requestInstallPackagesLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // 当权限请求返回后，检查是否获得了权限并继续安装流程
            if (context.packageManager.canRequestPackageInstalls()) {
                // 权限已获得，继续安装流程
                installApk()
            } else {
                // 用户没有授予权限，可以显示提示信息
                SnackbarManager.showSnackbar("需要安装权限才能更新应用")
            }
        }

        val languageOpts by settingViewModel.languages.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (dialogState.value) {
                is SettingViewModel.DialogState.About -> {
                    val urlHandler = LocalUriHandler.current
                    val strUrl = "https://github.com/coreycao/wikitok-android"
                    val link = LinkAnnotation.Url(
                        strUrl,
                        styles = TextLinkStyles(SpanStyle(color = Color.Blue))
                    ) {
                        val url = (it as LinkAnnotation.Url).url
                        urlHandler.openUri(url)
                    }
                    val annotatedText = buildAnnotatedString {
                        append(
                            "This App is opensource\n\nFind it on Github\n\n"
                        )
                        withLink(link) {
                            append(strUrl)
                        }
                    }
                    MessageDialog(annotatedText) {
                        settingViewModel.dismissDialog()
                    }
                }

                is SettingViewModel.DialogState.Option -> {
                    SelectOptionDialog(
                        options = languageOpts,
                        onOptionSelected = { option ->
                            settingViewModel.changeLanguage(option)
                            settingViewModel.dismissDialog()
                        },
                        onDismissRequest = {
                            settingViewModel.dismissDialog()
                        }
                    )
                }

                is SettingViewModel.DialogState.None -> {
                    Logger.d(message = "dismissDialog")
                }
            }

            LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                item {
                    SettingsItem(
                        Icons.Filled.Place,
                        stringResource(R.string.txt_setting_item_language),
                        stringResource(R.string.txt_setting_item_language_hint),
                        onClick = {
                            settingViewModel.showLangOptDialog()
                        }
                    )
                    SettingsItem(
                        Icons.AutoMirrored.Default.Send,
                        stringResource(R.string.txt_setting_item_export),
                        stringResource(R.string.txt_setting_item_export_hint),
                        onClick = {
                            settingViewModel.exportFavorites()
                        }
                    )
                    SettingsItem(
                        Icons.Filled.Refresh,
                        "${stringResource(R.string.txt_setting_item_version)}: ${BuildConfig.VERSION_NAME}",
                        stringResource(
                            if (downloadState.value is SettingViewModel.DownloadUiState.Completed) {
                                R.string.txt_setting_item_install_hint
                            } else {
                                R.string.txt_setting_item_version_hint
                            }
                        ),
                        onClick = {
                            when (downloadState.value) {
                                is SettingViewModel.DownloadUiState.Completed -> {
                                    if (!context.packageManager.canRequestPackageInstalls()) {
                                        // 如果没有权限，引导用户到设置页面开启
                                        val intent =
                                            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                                data = "package:${context.packageName}".toUri()
                                            }
                                        requestInstallPackagesLauncher.launch(intent)
                                    } else {
                                        installApk()
                                    }
                                }

                                else -> {
                                    settingViewModel.checkAppUpdate()
                                }
                            }

                        }
                    ) {
                        when (downloadState.value) {
                            is SettingViewModel.DownloadUiState.Downloading -> {
                                ProgressCircle(
                                    progress = (downloadState.value as SettingViewModel.DownloadUiState.Downloading).progress,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 5.dp
                                )
                            }

                            is SettingViewModel.DownloadUiState.Completed -> {
                                Icon(
                                    modifier = Modifier.size(28.dp),
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            }

                            else -> {
                                // do nothing
                            }
                        }

                    }
                    SettingsItem(
                        Icons.Default.Info,
                        stringResource(R.string.txt_setting_item_about),
                        stringResource(R.string.txt_setting_item_about_hint),
                        onClick = {
                            settingViewModel.showAboutDialog()
                        }
                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun SettingItemPreview() {
    MaterialTheme {
        SettingsItem(
            Icons.Filled.Done,
            "${stringResource(R.string.txt_setting_item_version)}: ${BuildConfig.VERSION_NAME}",
            stringResource(R.string.txt_setting_item_version_hint),
            trailingIcon = {
                ProgressCircle(
                    modifier = Modifier.size(28.dp),
                    progress = 0.75f,
                    strokeWidth = 5.dp
                )
            }
        )
    }
}

@Composable
fun SettingsItem(
    leadingIcon: Any,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                Logger.d(message = "click setting item: $title")
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (leadingIcon) {
            is ImageVector -> Icon(leadingIcon, contentDescription = null)
            is Painter -> Image(painter = leadingIcon, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 18.sp)
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        trailingIcon()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDialog(message: AnnotatedString, onDismiss: () -> Unit = {}) {
    BasicAlertDialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(message)
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Dismiss") }
            }
        }
    }
}

@Composable
fun SelectOptionDialog(
    options: List<Language>,
    onOptionSelected: (Language) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Languages",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(options.count()) { idx ->
                        val item = options[idx]
                        SelectOptionItem(
                            onItemSelected = { ->
                                onOptionSelected(item)
                                onDismissRequest
                            },
                            itemIconUrl = item.flag,
                            itemTitle = item.name,
                            isSelected = item.selected
                        )
                    }
                }

                TextButton(
                    onClick = {
                        onDismissRequest()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Dismiss") }
            }
        }
    }
}

@Composable
fun SelectOptionItem(
    onItemSelected: () -> Unit,
    itemIconUrl: String,
    itemTitle: String,
    isSelected: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                onItemSelected()
            }
    ) {
        NetworkImage(
            url = itemIconUrl,
            contentScale = ContentScale.FillBounds,
            contentDescription = itemTitle,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = itemTitle,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }

}