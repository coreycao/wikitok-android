package com.sy.wikitok.network

import com.sy.wikitok.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.flow.flow

/**
 * @author Yeung
 * @date 2025/3/18
 */
class ApiService(private val httpClient: HttpClient) {

    /**
     * 请求维基百科随机页面列表
     *
     * 常见参数补充说明：
     * - generator=random 会随机获取页面，常用于推荐系统
     * - grnnamespace=0 过滤非内容页面（如讨论页、用户页等）
     * - variant=zh-cn 会同时返回繁体标题，但正文内容会转换为简体中文
     * - pithumbsize 实际可能返回接近但不超过指定尺寸的缩略图
     * - exintro 与 exsentences 配合使用可控制摘要长度
     */
    fun observerWikiList(api: String, count: Int = 20) = flow {
        try {
            val response = httpClient.get(api) {
                url {
                    parameters.apply {
                        append("action", "query")      // 基础操作类型：数据查询
                        append("format", "json")       // 响应格式为JSON
                        append("generator", "random")  // 使用随机页面生成器
                        append("grnnamespace", "0")    // 限制在主命名空间（条目页面）
                        append("prop", "extracts|info|pageimages") // 要获取的属性：摘要/页面信息/图片
                        append("inprop", "url|varianttitles") // 页面信息包含URL和语言变体标题
                        append("grnlimit", count.toString())       // 获取20个随机页面
                        append("exintro", "1")         // 只提取引言部分（前导段落）
                        append("exlimit", "max")       // 提取所有匹配页面的摘要
                        append("exsentences", "5")     // 摘要最多包含5个句子
                        append("explaintext", "1")     // 返回纯文本而非HTML
                        append("piprop", "thumbnail")  // 请求页面缩略图
                        append("pithumbsize", "1080")   // 缩略图宽度800像素
                        append("origin", "*")          // 允许跨域请求
                        append("variant", "zh-cn")     // 使用简体中文变体
                    }
                }
            }
            Logger.i(tag = "ApiService", message = "requestWiki success")
            emit(Result.success(response))
        } catch (e: Exception) {
            Logger.e(tag = "ApiService", message = "requestWiki error: ${e.message}")
            emit(Result.failure(e))
        }
    }
}