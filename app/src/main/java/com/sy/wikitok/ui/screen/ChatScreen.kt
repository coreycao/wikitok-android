package com.sy.wikitok.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sy.wikitok.ui.component.MarkdownPreview
import org.koin.androidx.compose.koinViewModel

/**
 * @author Yeung
 * @date 2025/7/25
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(homeInnerPadding: PaddingValues, onBack: () -> Unit = {}) {

    val viewModel: ChatViewModel = koinViewModel()

    val msgState = viewModel.msgState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.padding(top = homeInnerPadding.calculateTopPadding()),
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
        bottomBar = {
            ChatInput(
                onSendMessage = { newMessageContent ->
                    newMessageContent.trimIndent()
                    viewModel.sendMessage(userMessageItem(newMessageContent))
                }
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ChatList(messages = msgState.value)
        }
    }
}

@Composable
fun ChatList(messages: List<MessageItem>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(messages) { index, message ->
            ChatItem(message)
        }
    }
}

@Composable
fun ChatItem(message: MessageItem) {
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
            ResponseError()
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
fun SenderLoading() {
    Card(
        shape = MaterialTheme.shapes.medium.copy(topEnd = CornerSize(0.dp))
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun ResponseText(content: String) {
    Column() {
        Icon(imageVector = Icons.Filled.Face, contentDescription = null)
        Spacer(modifier = Modifier.height(8.dp))
        MarkdownPreview(text = content)
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
fun ResponseError() {
    Column() {
        val sizeModifier = Modifier.size(24.dp)
        Icon(modifier = sizeModifier, imageVector = Icons.Filled.Face, contentDescription = null)
        Spacer(modifier = Modifier.height(8.dp))
        Icon(modifier = sizeModifier, imageVector = Icons.Filled.Warning, contentDescription = null)    }
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
                contentDescription = "发送"
            )
        }
    }
}