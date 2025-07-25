package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.sy.wikitok.data.repository.GenAIRepository
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * @author Yeung
 * @date 2025/7/25
 */
class ChatViewModel(private val genAIRepository: GenAIRepository) : ViewModel() {

    private val _msgState = MutableStateFlow(
        listOf(
            botMessageItem("你好，让我们深入聊聊这个你感兴趣的词条。")
        )
    )

    val msgState = _msgState.asStateFlow()
    fun sendMessage(message: MessageItem) {
        viewModelScope.launch {
            val messageList = _msgState.value + message + loadingMessageItem()
            _msgState.value = messageList
            val newMessage = genAIRepository.sendMessage(buildChatMessages(messageList))
            val updatedList = _msgState.value.toMutableList()
            updatedList.removeLastOrNull()
            newMessage.fold(
                onSuccess = { content ->
                    _msgState.value =
                        updatedList.apply { add(botMessageItem(content)) }
                }, onFailure = { e ->
                    Logger.e(message = "ai response error: ${e.message}")
                    _msgState.value =
                        updatedList.apply { add(errorMessageItem(e.message ?: "")) }
                }
            )
        }
    }
}

fun buildChatMessages(messageItemList: List<MessageItem>) =
    messageItemList.filter { item ->
        item.state is MessageItemState.User || item.state is MessageItemState.Bot
    }.map { item ->
        ChatMessage(
            role = if (item.state is MessageItemState.User) ChatRole.User else ChatRole.Assistant,
            content = item.content
        )
    }.toMutableList()

@OptIn(ExperimentalTime::class)
fun messageItemId() = Clock.System.now().nanosecondsOfSecond

fun botMessageItem(content: String): MessageItem =
    MessageItem(
        id = messageItemId(),
        content = content,
        state = MessageItemState.Bot
    )

fun userMessageItem(content: String): MessageItem =
    MessageItem(
        id = messageItemId(),
        content = content,
        state = MessageItemState.User
    )

fun loadingMessageItem(): MessageItem =
    MessageItem(
        id = messageItemId(),
        content = "",
        state = MessageItemState.Loading
    )

fun errorMessageItem(err: String): MessageItem =
    MessageItem(
        id = messageItemId(),
        content = err,
        state = MessageItemState.Error
    )

data class MessageItem(
    val id: Int,
    val content: String,
    val state: MessageItemState,
)

sealed class MessageItemState {
    object User : MessageItemState()
    object Bot : MessageItemState()
    object Error : MessageItemState()
    object Loading : MessageItemState()
}