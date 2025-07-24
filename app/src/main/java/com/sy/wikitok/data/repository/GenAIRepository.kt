package com.sy.wikitok.data.repository

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.chat.chatMessage
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.sy.wikitok.BuildConfig
import com.sy.wikitok.network.WikiApiService
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Duration.Companion.seconds

/**
 * @author Yeung
 * @date 2025/7/15
 */
class GenAIRepository(private val wikiApiService: WikiApiService) {

    companion object {
        const val MODEL_ID = "THUDM/GLM-4.1V-9B-Thinking"
        const val AI_HOST = "https://api.siliconflow.cn/v1/"
        const val SYSTEM_PROMPT = """
            你是一名小助手，善于洞察和总结。
            在接下来的对话中，请你根据我的问题，给出答案。
            """
    }

    private fun genAIClient() = OpenAI(
        token = BuildConfig.GENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds),
        host = OpenAIHost(AI_HOST),
    )

    private fun modelId() = ModelId(MODEL_ID)

    fun summaryFavorite(items: List<String>) = flow {
        emit(runCatching {
            val openai = genAIClient()
            val chatCompletionRequest = ChatCompletionRequest(
                model = modelId(),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = SYSTEM_PROMPT
                    ),

                    ChatMessage(
                        role = ChatRole.User,
                        content = """
                        我将给出你一份我在浏览维基百科时收藏的词条清单，
                        请你基于这些词条，总结出一份知识体系，谈谈你对我的洞察和了解。
                        我收藏的词条如下：
                        ${items.joinToString("\n")}
                                """.trimIndent()
                    )
                )
            )
            val completion: ChatCompletion = openai.chatCompletion(chatCompletionRequest)
            val responseMessage = completion.choices.first().message.content
            Logger.d(
                tag = "GenAIRepository",
                message = responseMessage.toString()
            )
            responseMessage ?: ""
        })
    }

    suspend fun genContentWithTool() {
        val aiClient = genAIClient()
        val modelId = modelId()

        val chatMessages = mutableListOf(chatMessage {
            role = ChatRole.User
            content = "What's the weather like in San Francisco, Tokyo, and Paris?"
        })

        // request with tools
        val request = chatCompletionRequest {
            model = modelId
            messages = chatMessages
            tools {
                function(
                    name = "currentWeather",
                    description = "Get the current weather in a given location",
                ) {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("location") {
                            put("type", "string")
                            put("description", "The city and state, e.g. San Francisco, CA")
                        }
                        putJsonObject("unit") {
                            put("type", "string")
                            putJsonArray("enum") {
                                add("celsius")
                                add("fahrenheit")
                            }
                        }
                    }
                    putJsonArray("required") {
                        add("location")
                    }
                }
            }
            toolChoice = ToolChoice.Auto // or ToolChoice.function("currentWeather")
        }

        val response = aiClient.chatCompletion(request)
        val message = response.choices.first().message
        chatMessages.append(message)
        for (toolCall in message.toolCalls.orEmpty()) {
            require(toolCall is ToolCall.Function) { "Tool call is not a function" }
            val functionResponse = toolCall.execute()
            chatMessages.append(toolCall, functionResponse)
        }
        val secondResponse = aiClient.chatCompletion(
            request = ChatCompletionRequest(model = modelId, messages = chatMessages)
        )
        print(secondResponse.choices.first().message.content.orEmpty())
    }

    private val availableFunctionTools = mapOf("fetchWikiItemDetail" to ::fetchWikiItemDetail)

    private fun fetchWikiItemDetail(args: JsonObject): String {
        // wikiApiService
        return ""
    }

    /**
     * Executes a function call and returns its result.
     */
    private fun ToolCall.Function.execute(): String {
        val functionToCall =
            availableFunctionTools[function.name] ?: error("Function ${function.name} not found")
        val functionArgs = function.argumentsAsJson()
        return functionToCall(functionArgs)
    }

    /**
     * Appends a chat message to a list of chat messages.
     */
    private fun MutableList<ChatMessage>.append(message: ChatMessage) {
        add(
            ChatMessage(
                role = message.role,
                content = message.content.orEmpty(),
                toolCalls = message.toolCalls,
                toolCallId = message.toolCallId,
            )
        )
    }

    /**
     * Appends a function call and response to a list of chat messages.
     */
    private fun MutableList<ChatMessage>.append(
        toolCall: ToolCall.Function,
        functionResponse: String
    ) {
        val message = ChatMessage(
            role = ChatRole.Tool,
            toolCallId = toolCall.id,
            name = toolCall.function.name,
            content = functionResponse
        )
        add(message)
    }
}