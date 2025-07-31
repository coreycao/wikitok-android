package com.sy.wikitok.data.repository

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.sy.wikitok.BuildConfig
import com.sy.wikitok.data.db.MessageDao
import com.sy.wikitok.data.db.MessageEntity
import com.sy.wikitok.data.db.MessageId
import com.sy.wikitok.data.db.MessageType
import com.sy.wikitok.data.model.WikiModel
import com.sy.wikitok.utils.Logger
import kotlin.time.Duration.Companion.seconds

/**
 * @author Yeung
 * @date 2025/7/28
 */

class AIChatRepository(private val messageDao: MessageDao, private val configRepository: ConfigRepository) {

    fun observableMessageList(wikiModel: WikiModel) = messageDao.observeMessages(wikiModel.id)

    suspend fun sayHello(wikiModel: WikiModel, messageContent: String) {
        messageDao.insertMessageIfNotExists(
            MessageEntity(
                wikiId = wikiModel.id,
                type = MessageType.BOT,
                content = messageContent.trimIndent(),
            )
        )
    }

    suspend fun retry(wikiModel: WikiModel, messageId: Long): Result<String> {

        messageDao.removeMessage(MessageId(messageId))

        return wikiChatCompletion(wikiModel)
    }

    suspend fun send(
        wikiModel: WikiModel,
        messageContent: String
    ): Result<String> {
        // Save user message to database
        val userMessage = MessageEntity(
            wikiId = wikiModel.id,
            type = MessageType.USER,
            content = messageContent.trimIndent(),
        )
        messageDao.insertMessage(userMessage)
        // Call AI chat completion
        return wikiChatCompletion(wikiModel)
    }

    suspend fun wikiChatCompletion(wikiModel: WikiModel): Result<String> {

        val aiConfig = configRepository.readChatAIConfig()

        val loadingMsg = MessageEntity(
            wikiId = wikiModel.id,
            type = MessageType.LOADING,
            content = "AI 正在思考中..."
        )
        val loadingMsgId = messageDao.insertMessage(loadingMsg)

        val systemMessage = ChatMessage(
            role = ChatRole.System,
            content = """
                ${aiConfig.systemPrompt}
                接下来要讨论的词条是：
                 词条名：${wikiModel.title}
                 词条简介：${wikiModel.content}
            """.trimIndent()
        )
        val messages = buildChatMessages(
            messageDao.readMessages(wikiModel.id)
        )
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId(aiConfig.model),
            messages = messages.toMutableList().apply {
                add(0, systemMessage) // Add system message at the beginning
            }
        )

        return try {

            val chatbot = OpenAI(
                token = BuildConfig.GENAI_API_KEY,
                timeout = Timeout(socket = 60.seconds),
                host = OpenAIHost(aiConfig.host)
            )

            val completion = chatbot.chatCompletion(chatCompletionRequest)
            val responseMessage = completion.choices.first().message.content
            messageDao.removeMessage(MessageId(loadingMsgId)) // Remove loading message
            if (responseMessage == null) {
                Logger.e("AIChatRepo chat error: responseMessage is null")
                val errorMsg = MessageEntity(
                    wikiId = wikiModel.id,
                    type = MessageType.ERROR,
                    content = "AI 响应错误，请稍后再试。"
                )
                messageDao.insertMessage(errorMsg) // Save error message to database
                Result.failure(Exception("response is null"))
            } else {
                // Save AI response to database
                val botMessage = MessageEntity(
                    wikiId = wikiModel.id,
                    type = MessageType.BOT,
                    content = responseMessage.trimIndent(),
                )
                messageDao.insertMessage(botMessage)
                Result.success(responseMessage)
            }
        } catch (e: Exception) {
            Logger.e("AIChatRepo chat error: ${e.message}")
            messageDao.removeMessage(MessageId(loadingMsgId)) // Remove loading message
            val errorMsg = MessageEntity(
                wikiId = wikiModel.id,
                type = MessageType.ERROR,
                content = "AI 响应错误，请稍后再试。"
            )
            messageDao.insertMessage(errorMsg) // Save error message to database
            Result.failure(e)
        }
    }

    fun buildChatMessages(messages: List<MessageEntity>) =
        messages.filter { item ->
            item.type == MessageType.USER || item.type == MessageType.BOT
        }.map { item ->
            ChatMessage(
                role = if (item.type == MessageType.USER) ChatRole.User else ChatRole.Assistant,
                content = item.content
            )
        }.toList()

}