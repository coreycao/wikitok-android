package com.sy.wikitok.ui.screen

import android.content.Intent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.BuildConfig
import com.sy.wikitok.R
import com.sy.wikitok.data.Langs
import com.sy.wikitok.data.Language
import com.sy.wikitok.ui.component.NetworkImage
import com.sy.wikitok.ui.screen.SettingViewModel.SettingDialogState.AppUpdateDialog
import com.sy.wikitok.utils.Logger
import com.sy.wikitok.utils.SnackbarManager
import org.koin.androidx.compose.koinViewModel

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        }) { innerPadding ->

        val dialogState = settingViewModel.settingDialogState.collectAsStateWithLifecycle()

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when (dialogState.value) {
                is AppUpdateDialog -> {
                    val checkState = (dialogState.value as AppUpdateDialog).checkedSuccess
                    if (checkState) {
                        val upgradeInfo = (dialogState.value as AppUpdateDialog).versionInfo!!
                        Logger.d(
                            tag = "SettingScreen",
                            message = "check upgrade success, newVersion: ${upgradeInfo.hasUpdate}"
                        )
                        // TODO: show upgrade dialog
                        SnackbarManager.showSnackbar(
                            "new version found: ${upgradeInfo.latestVersion}",
                            actionLabel = "Upgrade",
                            onAction = settingViewModel::showAboutMessageDialog
                        )
                    } else {
                        Logger.d(tag = "SettingScreen", message = "check upgrade failed")
                        SnackbarManager.showSnackbar(stringResource(R.string.snakebar_uptodate))
                    }
                }

                is SettingViewModel.SettingDialogState.AboutMessageDialog -> {
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

                is SettingViewModel.SettingDialogState.LanguageOption -> {
                    val langOptions = Langs.values.toList()
                    SelectOptionDialog(
                        options = langOptions,
                        onOptionSelected = { option ->
                            settingViewModel.changeLanguage(option)
                            settingViewModel.dismissDialog()
                        },
                        onDismissRequest = {
                            settingViewModel.dismissDialog()
                        }
                    )
                }

                is SettingViewModel.SettingDialogState.ExportFavorite -> {
                    (dialogState.value as SettingViewModel.SettingDialogState.ExportFavorite)
                        .result.fold(onSuccess = {
                            if (it.isBlank()) {
                                SnackbarManager.showSnackbar("Export failed, you have no favorite wikis.")
                            } else {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, it)
                                }
                                LocalContext.current.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        "Share"
                                    )
                                )
                            }
                        }, onFailure = {
                            Logger.d(tag = "SettingScreen", message = "export error: $it")
                            SnackbarManager.showSnackbar("Export failed, try again")
                        })
                }

                is SettingViewModel.SettingDialogState.None -> {
                    // do nothing, just dismiss the dialogs.
                }
            }

            LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                item {
                    SettingsItem(
                        Icons.Filled.Place,
                        stringResource(R.string.txt_setting_item_language),
                        stringResource(R.string.txt_setting_item_language_hint)
                    ) {
                        settingViewModel.showLanguageOptionDialog()
                    }
                    SettingsItem(
                        Icons.AutoMirrored.Default.Send,
                        stringResource(R.string.txt_setting_item_export),
                        stringResource(R.string.txt_setting_item_export_hint)
                    ) {
                        settingViewModel.exportFavorite()
                    }
                    SettingsItem(
                        Icons.Filled.Refresh,
                        "${stringResource(R.string.txt_setting_item_version)}: ${BuildConfig.VERSION_NAME}",
                        stringResource(R.string.txt_setting_item_version_hint)
                    ) {
                        settingViewModel.checkAppUpdate()
                    }
                    SettingsItem(
                        Icons.Default.Info,
                        stringResource(R.string.txt_setting_item_about),
                        stringResource(R.string.txt_setting_item_about_hint)
                    ) {
                        settingViewModel.showAboutMessageDialog()
                    }
                }
            }

        }
    }
}

@Composable
fun SettingsItem(icon: Any, title: String, subtitle: String, action: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                Logger.d(tag = "SettingScreen", message = "click setting item: $title")
                action()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (icon) {
            is ImageVector -> Icon(icon, contentDescription = null)
            is Painter -> Image(painter = icon, contentDescription = null)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageDialog(message: AnnotatedString, onDismiss: () -> Unit = {}) {
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
private fun SelectOptionDialog(
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
                            itemTitle = item.name
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
fun SelectOptionItem(onItemSelected: () -> Unit, itemIconUrl: String, itemTitle: String) {
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
            style = MaterialTheme.typography.bodyLarge
        )
    }

}