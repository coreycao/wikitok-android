package com.sy.wikitok.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.ui.component.Collapsible
import com.sy.wikitok.ui.component.MarkdownPreview
import com.sy.wikitok.utils.Logger
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * @author Yeung
 * @date 2025/7/25
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    homeInnerPadding: PaddingValues,
    wikiInfo: WikiModel,
    onBack: () -> Unit = {}
) {

    val chatViewModel: ChatViewModel = koinViewModel<ChatViewModel>(
        parameters = { parametersOf(wikiInfo) }
    )

    val msgState = chatViewModel.chatUiState.collectAsStateWithLifecycle()

    val chatListState = chatViewModel.chatListState

    val isFABVisible by remember(chatListState) {
        derivedStateOf {
            chatListState.firstVisibleItemIndex > 0
        }
    }

    LaunchedEffect(Unit) {
        Logger.d("ChatScreen LaunchedEffect")
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Wiki Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isFABVisible) {
                FloatingActionButton(
                    onClick = {
                        chatViewModel.scrollToTop()
                    }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = null
                    )
                }
            }
        },
        bottomBar = {
            ChatInput(
                onSendMessage = { messageContent ->
                    chatViewModel.sendMessage(messageContent)
                }
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            ChatList(messages = msgState.value, chatViewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatList(messages: List<MessageItem>, viewModel: ChatViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = viewModel.chatListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = messages, key = { messageItem -> messageItem.id }) { message ->
            when (message.state) {
                is MessageItemState.Bot -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ResponseText(message.content)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                is MessageItemState.User -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        SenderText(message.content)
                    }
                }

                is MessageItemState.Loading -> {
                    ResponseLoading()
                }

                is MessageItemState.Error -> {
                    ResponseError {
                        viewModel.retryMessage(message.id)
                    }
                }
            }

        }
    }
}

@Composable
fun SenderText(content: String) {
    Card(
        shape = MaterialTheme.shapes.medium.copy(topEnd = CornerSize(0.dp))
    ) {
        Text(
            content,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ResponseText(content: String) {
    Column() {
        Icon(imageVector = Icons.Filled.Face, contentDescription = null)
        Spacer(modifier = Modifier.height(8.dp))
        Collapsible(collapsedHeight = 128.dp, initiallyExpanded = true) {
            MarkdownPreview(text = content)
        }
    }
}

@Composable
fun ResponseLoading() {
    Column() {
        val sizeModifier = Modifier.size(24.dp)
        Icon(modifier = sizeModifier, imageVector = Icons.Filled.Face, contentDescription = null)
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator(
            modifier = sizeModifier,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun ResponseError(onRetry: () -> Unit = {}) {
    Column() {
        val sizeModifier = Modifier.size(24.dp)
        Icon(modifier = sizeModifier, imageVector = Icons.Filled.Face, contentDescription = null)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text(text = "请求失败")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                modifier = sizeModifier,
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color.Red
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            modifier = sizeModifier.clickable(
                onClick = {
                    Logger.d(tag = "ChatScreen", message = "click retry button")
                    onRetry()
                }
            ), imageVector = Icons.Filled.Refresh, contentDescription = null)
    }
}

@Composable
fun ChatInput(
    onSendMessage: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .height(96.dp)
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("输入消息...") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                }
            })
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "发送",
                tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}