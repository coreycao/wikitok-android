package com.sy.wikitok.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sy.wikitok.data.db.MessageType
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.data.repository.AIChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * @author Yeung
 * @date 2025/7/25
 */
class ChatViewModel(
    private val aiChatRepository: AIChatRepository,
    private val wikiModel: WikiModel
) : ViewModel() {

    init {
        viewModelScope.launch {
            aiChatRepository.sayHello(
                wikiModel,
                """
                你好，让我们来深入聊聊这个你感兴趣的词条：**${wikiModel.title}**
                你有什么想更深入了解的？
            """.trimIndent()
            )
        }
    }

    val chatUiState = aiChatRepository.observableMessageList(wikiModel)
        .map {
            it.map { messageEntity ->
                MessageItem(
                    id = messageEntity.id,
                    content = messageEntity.content,
                    state = when (messageEntity.type) {
                        MessageType.USER -> MessageItemState.User
                        MessageType.BOT -> MessageItemState.Bot
                        MessageType.ERROR -> MessageItemState.Error
                        MessageType.LOADING -> MessageItemState.Loading
                    }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun sendMessage(messageContent: String) {
        viewModelScope.launch {
            aiChatRepository.send(wikiModel, messageContent)
        }
    }

    fun retryMessage(messageId: Long) {
        viewModelScope.launch {
            aiChatRepository.retry(wikiModel, messageId)
        }
    }
}

data class MessageItem(
    val id: Long,
    val content: String,
    val state: MessageItemState,
)

sealed class MessageItemState {
    object User : MessageItemState()
    object Bot : MessageItemState()
    object Error : MessageItemState()
    object Loading : MessageItemState()
}