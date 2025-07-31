package com.sy.wikitok.ui.component

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

/**
 * @author Yeung
 * @date 2025/7/24
 */

/**
 * 可显示Markdown格式文本的可组合组件
 * 支持链接点击跳转、粗体、斜体、代码块、标题等Markdown语法
 *
 * @param modifier 修饰符，用于设置组件的布局和样式属性
 * @param text 需要解析和显示的Markdown格式文本
 */
@Composable
fun MarkdownPreview(
    modifier: Modifier = Modifier,
    text: String,
) {
    // 获取当前上下文，用于启动链接跳转的Intent
    val context = LocalContext.current
    // 将Markdown文本解析为带样式的AnnotatedString
    val annotatedString = parseMarkdownToAnnotatedString(text)

    // 存储文本布局结果，用于处理点击事件时确定点击位置
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        modifier = modifier
            .fillMaxWidth()
            // 添加点击手势检测，用于处理链接点击
            .pointerInput(Unit) {
                detectTapGestures { tapOffsetPosition ->
                    val layoutResult = textLayoutResult ?: return@detectTapGestures
                    // 根据点击位置获取文本偏移量
                    val position = layoutResult.getOffsetForPosition(tapOffsetPosition)
                    // 查找点击位置是否有URL注解
                    annotatedString
                        .getStringAnnotations(start = position, end = position)
                        .firstOrNull { it.tag == "URL" }
                        ?.let { annotation ->
                            // 启动浏览器打开链接
                            val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                            context.startActivity(intent)
                        }
                }
            },
        // 保存文本布局结果用于后续处理
        onTextLayout = { result ->
            textLayoutResult = result
        }
    )


}

/**
 * 将Markdown格式文本解析为带样式的AnnotatedString
 * 支持的Markdown语法包括：
 * - 链接: [text](url)
 * - 粗体: **text**
 * - 斜体: *text*
 * - 代码块: ```code```
 * - 行内代码: `code`
 * - 标题: # Heading, ## Heading, ### Heading 等，支持1-6级标题
 * - 列表项: - item
 * - 引用块: > quote
 *
 * @param markdown 原始Markdown格式文本
 * @return 解析后的带样式的AnnotatedString对象
 */
@Composable
fun parseMarkdownToAnnotatedString(markdown: String): AnnotatedString {
    // 定义各种Markdown语法的正则表达式
    val linkRegex = """\[(.*?)\]\((.*?)\)""".toRegex()           // 链接匹配规则: [text](url)
    val boldRegex = """\*\*(.*?)\*\*""".toRegex()                // 粗体匹配规则: **text**
    val italicRegex = """\*(.*?)\*""".toRegex()                  // 斜体匹配规则: *text*
    val codeBlockRegex = """```([\s\S]*?)```""".toRegex()        // 代码块匹配规则: ```code```
    val inlineCodeRegex = """`(.*?)`""".toRegex()                // 行内代码匹配规则: `code`
    val headingRegex = """^(#{1,6})\s*(.*)""".toRegex(RegexOption.MULTILINE)  // 标题匹配规则: #到###### 
    val listRegex = """^- (.*)""".toRegex(RegexOption.MULTILINE) // 列表项匹配规则: - item
    val blockquoteRegex = """^>\s+(.*)""".toRegex(RegexOption.MULTILINE)  // 引用块匹配规则: > quote

    // 创建用于存储解析出的Markdown标记的列表
    val tokens = mutableListOf<MarkdownToken>()

    /**
     * 辅助函数：查找并添加匹配的Markdown标记到tokens列表中
     *
     * @param pattern 正则表达式模式
     * @param type 标记类型
     * @param groupCount 捕获组数量
     */
    fun addMatches(pattern: Regex, type: TokenType, groupCount: Int) {
        pattern.findAll(markdown).forEach { result ->
            // 提取捕获组内容
            val matchedGroups = (1..groupCount).map { i -> result.groups[i]?.value ?: "" }
            tokens += MarkdownToken(
                type = type,
                start = result.range.first,     // 匹配开始位置
                end = result.range.last + 1,    // 匹配结束位置
                groups = matchedGroups          // 捕获组内容列表
            )
        }
    }

    // 收集各种Markdown语法标记
    addMatches(codeBlockRegex, TokenType.CODE_BLOCK, 1)
    addMatches(inlineCodeRegex, TokenType.INLINE_CODE, 1)
    addMatches(linkRegex, TokenType.LINK, 2)
    addMatches(boldRegex, TokenType.BOLD, 1)
    addMatches(italicRegex, TokenType.ITALIC, 1)
    addMatches(headingRegex, TokenType.HEADING, 2)
    addMatches(listRegex, TokenType.LIST, 1)
    addMatches(blockquoteRegex, TokenType.BLOCKQUOTE, 1)

    // 按照在文本中的位置对标记进行排序
    tokens.sortBy { it.start }

    // 创建AnnotatedString构建器用于构建最终的带样式文本
    val builder = AnnotatedString.Builder()
    var currentIndex = 0  // 当前处理到的文本位置

    /**
     * 辅助函数：添加未被标记覆盖的普通文本
     *
     * @param upTo 需要添加到的位置
     */
    fun appendGapText(upTo: Int) {
        if (currentIndex < upTo) {
            builder.append(markdown.substring(currentIndex, upTo))
            currentIndex = upTo
        }
    }

    // 遍历所有解析出的标记并应用相应样式
    for (token in tokens) {
        // 如果当前标记与之前处理的标记重叠，则跳过
        if (token.start < currentIndex) continue
        // 先处理标记前的普通文本
        appendGapText(token.start)

        // 根据标记类型应用不同样式
        when (token.type) {
            TokenType.CODE_BLOCK -> {
                val codeContent = token.groups[0].trim()  // 获取代码内容并去除首尾空格
                val styleStart = builder.length
                builder.append(codeContent)
                // 为代码块添加背景色、字体等样式
                builder.addStyle(
                    SpanStyle(
                        background = Color(0xFFEFEFEF),      // 浅灰色背景
                        color = Color(0xFF333333),           // 深灰色文字
                        fontSize = 16.sp,                    // 字体大小
                        fontFamily = FontFamily.Monospace    // 等宽字体
                    ),
                    styleStart,
                    builder.length
                )
            }

            TokenType.INLINE_CODE -> {
                val codeContent = token.groups[0]  // 获取行内代码内容
                val styleStart = builder.length
                builder.append(codeContent)
                // 为行内代码添加背景色、字体等样式
                builder.addStyle(
                    SpanStyle(
                        background = Color.LightGray,        // 浅灰色背景
                        fontSize = 14.sp,                    // 字体大小
                        fontFamily = FontFamily.Monospace    // 等宽字体
                    ),
                    styleStart,
                    builder.length
                )
            }

            TokenType.LINK -> {
                val (linkText, linkUrl) = token.groups  // 分别获取链接文本和URL
                val styleStart = builder.length
                builder.append(linkText)
                // 为链接添加蓝色字体和下划线样式
                builder.addStyle(
                    SpanStyle(
                        color = Color.Blue,                           // 蓝色字体
                        textDecoration = TextDecoration.Underline     // 下划线
                    ),
                    styleStart,
                    builder.length
                )
                // 添加URL注解，用于点击事件处理
                builder.addStringAnnotation(
                    tag = "URL",
                    annotation = linkUrl,
                    start = styleStart,
                    end = builder.length
                )
            }

            TokenType.BOLD -> {
                val boldContent = token.groups[0]  // 获取粗体内容
                val styleStart = builder.length
                builder.append(boldContent)
                // 为粗体文本添加加粗样式
                builder.addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),  // 加粗
                    styleStart,
                    builder.length
                )
            }

            TokenType.ITALIC -> {
                val italicContent = token.groups[0]  // 获取斜体内容
                val styleStart = builder.length
                builder.append(italicContent)
                // 为斜体文本添加斜体样式
                builder.addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic),  // 斜体
                    styleStart,
                    builder.length
                )
            }

            TokenType.HEADING -> {
                val headingLevel = token.groups[0].length // 获取标题级别 (#的数量)
                val headingText = token.groups[1]         // 获取标题文本
                val styleStart = builder.length
                builder.append(headingText)
                // 根据标题级别应用不同样式，支持1-6级标题
                builder.addStyle(
                    SpanStyle(
                        fontSize = when (headingLevel) {
                            1 -> 28.sp
                            2 -> 24.sp
                            3 -> 22.sp
                            4 -> 20.sp
                            5 -> 18.sp
                            else -> 16.sp // 6级标题及默认情况
                        },
                        fontWeight = when (headingLevel) {
                            1 -> FontWeight.Bold
                            2 -> FontWeight.Bold
                            3 -> FontWeight.Medium
                            else -> FontWeight.Normal
                        },
                        color = when (headingLevel) {
                            1 -> MaterialTheme.colorScheme.primary
                            2 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    styleStart,
                    builder.length
                )
            }

            TokenType.LIST -> {
                val listItem = token.groups[0]  // 获取列表项内容
                // 添加带项目符号的列表项
                builder.append("• $listItem\n")
            }

            TokenType.BLOCKQUOTE -> {
                val quoteText = token.groups[0]  // 获取引用文本
                val styleStart = builder.length
                builder.append(quoteText)
                // 为引用块添加背景色和斜体样式
                builder.addStyle(
                    SpanStyle(
                        background = Color(0xFFE0E0E0),      // 灰色背景
                        fontStyle = FontStyle.Italic         // 斜体
                    ),
                    styleStart,
                    builder.length
                )
                builder.append("\n")  // 添加换行
            }
        }
        // 更新当前处理位置到标记结束位置
        currentIndex = token.end
    }

    // 处理剩余的普通文本
    appendGapText(markdown.length)

    // 返回构建完成的AnnotatedString
    return builder.toAnnotatedString()
}

/**
 * 表示一个Markdown标记的数据类
 *
 * @property type 标记类型
 * @property start 标记在原文本中的起始位置
 * @property end 标记在原文本中的结束位置
 * @property groups 正则表达式捕获组的内容列表
 */
private data class MarkdownToken(
    val type: TokenType,
    val start: Int,
    val end: Int,
    val groups: List<String>
)

/**
 * Markdown标记类型枚举
 */
private enum class TokenType {
    CODE_BLOCK,    // 代码块
    INLINE_CODE,   // 行内代码
    LINK,          // 链接
    BOLD,          // 粗体
    ITALIC,        // 斜体
    HEADING,       // 标题
    LIST,          // 列表项
    BLOCKQUOTE     // 引用块
}