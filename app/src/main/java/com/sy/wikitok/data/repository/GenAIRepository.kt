package com.sy.wikitok.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
import com.sy.wikitok.data.repository.ConfigRepository.Companion.AI_HOST
import com.sy.wikitok.data.repository.ConfigRepository.Companion.MODEL_ID
import com.sy.wikitok.network.WikiApiService
import kotlinx.coroutines.flow.firstOrNull
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
class GenAIRepository(
    private val wikiApiService: WikiApiService,
    private val configRepository: ConfigRepository,
    private val dataStore: DataStore<Preferences>
) {

    fun getAISummary(favList: List<String>) = flow {
        val favListHash = favList.hashCode()
        val summaryCacheKey = stringPreferencesKey("summary_$favListHash")
        // 从 dataStore 中读取缓存
        val cachedSummary = dataStore.data.firstOrNull()?.get(summaryCacheKey)

        if (cachedSummary != null) {
            // 如果有缓存，直接返回
            emit(Result.success(cachedSummary))
        } else {
            // 如果没有缓存，请求网络
            emit(runCatching {
                val summaryResponse = requestSummaryFromNetwork(favList)
                // 移除旧的不匹配的缓存，并保存新的缓存到 dataStore
                dataStore.edit { preferences ->
                    preferences.clear()
                    preferences[summaryCacheKey] = summaryResponse
                }
                summaryResponse
            })
        }
    }

    private suspend fun requestSummaryFromNetwork(items: List<String>): String {
        val aiConfig = configRepository.readSummaryAIConfig()
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId(aiConfig.model),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = aiConfig.systemPrompt
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = """
                    ${aiConfig.bizPrompt}
                    ${items.joinToString("\n")}                
                    """.trimIndent()
                )
            )
        )
        val aiClient = OpenAI(
            token = BuildConfig.GENAI_API_KEY,
            timeout = Timeout(socket = 60.seconds),
            host = OpenAIHost(aiConfig.host),
        )
        val completion: ChatCompletion = aiClient.chatCompletion(chatCompletionRequest)
        return completion.choices.first().message.content ?: ""
    }

   /** gen ai with function tools, still in progress**/

    private fun genAIClient() = OpenAI(
        token = BuildConfig.GENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds),
        host = OpenAIHost(AI_HOST),
    )

    private fun modelId() = ModelId(MODEL_ID)

    private val chatbot = OpenAI(
        token = BuildConfig.GENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds),
        host = OpenAIHost(AI_HOST),
    )
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