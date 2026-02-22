package com.example.voicebill.data.repository

import com.example.voicebill.data.remote.ChatCompletionRequest
import com.example.voicebill.data.remote.ChatMessage
import com.example.voicebill.data.remote.DeepSeekApi
import com.example.voicebill.di.ApiKeyValidator
import com.example.voicebill.di.SecurePrefs
import com.example.voicebill.domain.model.BillInfo
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.BillParserRepository
import com.example.voicebill.domain.repository.CategoryRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillParserRepositoryImpl @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val securePrefs: SecurePrefs,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : BillParserRepository {

    private val gson = Gson()
    private val dateTextFormatter = DateTimeFormatter.ofPattern(DATE_TEXT_PATTERN)

    // 中文数字到阿拉伯数字的映射
    private val chineseNumbers = mapOf(
        "零" to 0, "一" to 1, "二" to 2, "三" to 3, "四" to 4,
        "五" to 5, "六" to 6, "七" to 7, "八" to 8, "九" to 9,
        "十" to 10, "百" to 100, "千" to 1000, "万" to 10000,
        "两" to 2, "壹" to 1, "贰" to 2, "叁" to 3, "肆" to 4,
        "伍" to 5, "陆" to 6, "柒" to 7, "捌" to 8, "玖" to 9,
        "拾" to 10, "佰" to 100, "仟" to 1000, "萬" to 10000
    )

    // 分类关键词映射
    private val categoryKeywords = mapOf(
        "餐饮" to listOf("吃", "饭", "火锅", "烧烤", "外卖", "餐厅", "早餐", "午餐", "晚餐", "零食", "水果", "奶茶", "咖啡"),
        "交通" to listOf("车", "加油", "地铁", "公交", "打车", "出租", "停车", "保养", "维修", "过路", "高铁", "火车", "飞机"),
        "购物" to listOf("买", "购物", "淘宝", "京东", "快递", "衣服", "鞋", "包", "化妆品", "电子产品"),
        "工资" to listOf("工资", "薪资", "月薪", "年薪", "奖金"),
        "红包" to listOf("红包", "压岁钱", "礼金")
    )

    // 收入关键词
    private val incomeKeywords = listOf("收入", "赚", "收到", "发工资", "领工资", "进账")

    override suspend fun parseBillText(text: String): BillInfo {
        // 检查是否有 API Key
        val apiKey = securePrefs.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return BillInfo(
                amountCents = null,
                categoryName = null,
                type = null,
                date = null,
                parseSuccess = false,
                errorMessage = "请先在设置中配置 DeepSeek API Key"
            )
        }

        try {
            // 先做本地解析，提供金额/分类/备注候选
            val localResult = parseLocally(text)
            // 时间字段强制走 AI，避免本地粗解析导致偏移
            val apiResult = parseWithApi(text, apiKey)
            if (!apiResult.parseSuccess) {
                return apiResult
            }
            return mergeLocalAndApiResult(localResult, apiResult)
        } catch (e: Exception) {
            return BillInfo(
                amountCents = null,
                categoryName = null,
                type = null,
                date = null,
                parseSuccess = false,
                errorMessage = "解析失败: ${e.message}"
            )
        }
    }

    private fun parseLocally(text: String): BillInfo {
        // 解析金额
        val amountCents = parseAmount(text) ?: return BillInfo(
            amountCents = null,
            categoryName = null,
            type = null,
            date = null,
            parseSuccess = false
        )

        // 解析类型（收入/支出）
        val isIncome = incomeKeywords.any { text.contains(it) }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // 解析分类
        val categoryName = parseCategory(text, type)

        // 解析备注（去除金额和分类相关词汇后的剩余部分）
        val note = parseNote(text)

        return BillInfo(
            amountCents = amountCents,
            categoryName = categoryName,
            type = type,
            // 时间字段由 AI 统一给出，避免本地规则误判
            date = null,
            note = note,
            parseSuccess = true
        )
    }

    private fun parseAmount(text: String): Long? {
        // 匹配 "XX元"、"XX块"、"XX" 等模式
        val patterns = listOf(
            // 阿拉伯数字 + 元/块/圆
            Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(元|块|圆)"),
            // 阿拉伯数字单独（可能是金额）
            Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)$"),
            // 中文数字
            Pattern.compile("([零一二三四五六七八九十百千万亿]+)\\s*(元|块|圆)?")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val amountStr = matcher.group(1) ?: continue

                // 尝试解析为数字
                val amount = try {
                    amountStr.toDouble()
                } catch (e: NumberFormatException) {
                    // 尝试解析中文数字
                    parseChineseNumber(amountStr)?.toDouble()
                } ?: continue

                // 转换为分
                return Math.round(amount * 100)
            }
        }

        return null
    }

    private fun parseChineseNumber(chinese: String): Double? {
        if (chinese.isEmpty()) return null

        var result = 0.0
        var temp = 0.0
        var unit = 1.0

        for (char in chinese) {
            val num = chineseNumbers[char.toString()]?.toDouble()
            when {
                num == null -> continue
                num >= 10 -> {
                    unit = num
                    if (temp > 0) {
                        result += temp * unit
                        temp = 0.0
                    } else {
                        result += unit
                    }
                    unit = 1.0
                }
                else -> {
                    temp = num
                }
            }
        }

        result += temp * unit
        return if (result > 0) result else null
    }

    private fun parseCategory(text: String, type: TransactionType): String {
        // 根据类型选择对应的关键词映射
        val keywords = if (type == TransactionType.INCOME) {
            mapOf(
                "工资" to listOf("工资", "薪资", "月薪"),
                "红包" to listOf("红包", "压岁钱"),
                "其他收入" to emptyList()
            )
        } else {
            categoryKeywords
        }

        for ((category, words) in keywords) {
            if (words.isEmpty() || words.any { text.contains(it) }) {
                return category
            }
        }

        return "其他"
    }

    private fun parseNote(text: String): String? {
        // 简单处理：去除金额和常见关键词后的剩余部分
        var note = text
            .replace(Regex("[0-9]+(?:\\.[0-9]+)?\\s*(元|块|圆)"), "")
            .replace(Regex("[零一二三四五六七八九十百千万亿]+\\s*(元|块|圆)"), "")
            .trim()

        // 去除时间相关词汇
        val timeWords = listOf("今天", "昨天", "前天", "明天", "后天", "上周", "下周", "周一", "周二", "周三", "周四", "周五", "周六", "周日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日", "星期天")
        for (word in timeWords) {
            note = note.replace(word, "")
        }

        // 去除分类关键词
        val categoryWords = listOf("吃", "花", "买", "给", "用", "了", "消费", "支出", "收入", "赚")
        for (word in categoryWords) {
            note = note.replace(word, "")
        }

        return note.trim().ifEmpty { null }
    }

    private suspend fun parseWithApi(text: String, apiKey: String): BillInfo {
        val validation = ApiKeyValidator.validate(apiKey)
        if (!validation.isValid) {
            return BillInfo(
                amountCents = null,
                categoryName = null,
                type = null,
                date = null,
                parseSuccess = false,
                errorMessage = validation.errorMessage ?: "请先在设置中配置正确的 DeepSeek API Key"
            )
        }

        val zoneId = ZoneId.of(DEFAULT_TIMEZONE)
        val currentTime = LocalDateTime.now(clock.withZone(zoneId))
        val dateAnchors = buildDateAnchors(currentTime)

        // 动态获取分类列表
        val categories = categoryRepository.getAllCategories().first()
        val expenseCategories = categories
            .filter { !it.isIncome && !it.isUncategorized }
            .joinToString("、") { it.name }
        val incomeCategories = categories
            .filter { it.isIncome && !it.isUncategorized }
            .joinToString("、") { it.name }

        val systemPrompt = buildSystemPrompt(expenseCategories, incomeCategories)
        val userPrompt = buildUserPrompt(text, currentTime, dateAnchors)

        val request = ChatCompletionRequest(
            model = "deepseek-chat",
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(role = "user", content = userPrompt)
            ),
            temperature = 0.1,
            maxTokens = 500
        )

        val response = deepSeekApi.createChatCompletion(
            authorization = "Bearer ${validation.normalizedKey}",
            request = request
        )

        val content = response.choices.firstOrNull()?.message?.content ?: return BillInfo(
            amountCents = null,
            categoryName = null,
            type = null,
            date = null,
            parseSuccess = false,
            errorMessage = "API 返回为空"
        )

        return try {
            // 尝试解析 JSON
            parseApiResponse(content)
        } catch (e: Exception) {
            // 解析失败，尝试提取 JSON
            extractJsonFromResponse(content) ?: BillInfo(
                amountCents = null,
                categoryName = null,
                type = null,
                date = null,
                parseSuccess = false,
                errorMessage = "解析 API 响应失败: ${e.message}"
            )
        }
    }

    private fun parseApiResponse(content: String): BillInfo {
        try {
            val cleanedContent = stripCodeFence(content)
            val json = gson.fromJson(cleanedContent, Map::class.java)

            val amountCents = (json["amountCents"] as? Number)?.toLong()
            val categoryName = json["categoryName"] as? String
            val typeStr = json["type"] as? String
            val dateText = json["dateText"] as? String
            val timezone = json["timezone"] as? String
            val note = (json["note"] as? String)?.ifBlank { null }

            val type = when (typeStr) {
                "income" -> TransactionType.INCOME
                "expense" -> TransactionType.EXPENSE
                else -> null
            }
            // dateText 解析失败时兜底当前时间，保证可保存
            val dateTimestamp = parseDateTextToEpochMillis(dateText, timezone)
                ?: clock.instant().toEpochMilli()

            return BillInfo(
                amountCents = amountCents,
                categoryName = categoryName,
                type = type,
                date = dateTimestamp,
                note = note,
                parseSuccess = true
            )
        } catch (e: JsonSyntaxException) {
            throw e
        }
    }

    private fun extractJsonFromResponse(content: String): BillInfo? {
        // 尝试提取 JSON 对象
        val jsonPattern = Pattern.compile("\\{[^}]+\\}")
        val matcher = jsonPattern.matcher(stripCodeFence(content))

        while (matcher.find()) {
            val jsonStr = matcher.group()
            try {
                return parseApiResponse(jsonStr)
            } catch (e: Exception) {
                continue
            }
        }

        return null
    }

    private fun parseDateTextToEpochMillis(dateText: String?, timezone: String?): Long? {
        if (dateText.isNullOrBlank()) return null

        val zoneId = runCatching {
            ZoneId.of(timezone?.ifBlank { DEFAULT_TIMEZONE } ?: DEFAULT_TIMEZONE)
        }.getOrElse {
            ZoneId.of(DEFAULT_TIMEZONE)
        }

        val localDateTime = runCatching {
            LocalDateTime.parse(dateText, dateTextFormatter)
        }.getOrNull() ?: return null

        return localDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    private fun mergeLocalAndApiResult(localResult: BillInfo, apiResult: BillInfo): BillInfo {
        val mergedAmount = localResult.amountCents ?: apiResult.amountCents
        val mergedType = apiResult.type ?: localResult.type
        val mergedCategory = apiResult.categoryName ?: localResult.categoryName
        val mergedNote = apiResult.note ?: localResult.note
        val parseSuccess = mergedAmount != null

        return apiResult.copy(
            amountCents = mergedAmount,
            type = mergedType,
            categoryName = mergedCategory,
            note = mergedNote,
            parseSuccess = parseSuccess,
            errorMessage = if (parseSuccess) null else "无法识别金额，请补充金额后重试"
        )
    }

    private fun buildSystemPrompt(expenseCategories: String, incomeCategories: String): String {
        return """
            你是中文记账语句解析器。只输出一个 JSON 对象，不要解释，不要代码块，不要 Markdown。

            输出字段：
            - amountCents: 金额（分，整数）
            - categoryName: 分类名，无法确定填"未分类"。支出分类优先从：$expenseCategories；收入分类优先从：$incomeCategories。
            - type: "income" 或 "expense"
            - dateText: 绝对时间字符串，格式固定 yyyy-MM-dd HH:mm:ss
            - timezone: 固定 "Asia/Shanghai"
            - note: 备注，可空字符串

            硬规则：
            1) 先基于“当前时间锚点”计算绝对日期，再填 dateText。
            2) 周起始是周一，周结束是周日。
            3) 若用户含“上周X”：目标日期 = 上周周一 + (X的星期索引-1)天。
            4) 若用户含“下周X”：目标日期 = 下周周一 + (X的星期索引-1)天。
            5) 若用户仅含“周X/星期X”（无上/下）：
               - 若X=当前星期，则取今天；
               - 若X在当前星期之前，则取本周该日；
               - 否则取上周该日。
            6) 只有日期词无时段词：保留当前时分。
            7) 有时段词则覆盖时分，分钟秒=00：
               凌晨=03:00，早上/清晨=08:00，上午=10:00，中午=12:00，下午=16:00，傍晚=18:00，晚上/今晚/昨晚/夜里=19:00，午夜=00:00。
            8) 如果语句没有明确年份词（如“去年/明年/2025年”），不得随意切换年份。
            9) 输出前必须自检：
               - 含“上周”时，dateText 必须落在上周区间；
               - 含“下周”时，dateText 必须落在下周区间；
               - 不满足则重新计算后再输出。
            10) 只输出 JSON，不允许输出额外字段和解释文本。
        """.trimIndent()
    }

    private fun buildUserPrompt(text: String, now: LocalDateTime, anchors: DateAnchors): String {
        return """
            当前时间：${now.format(dateTextFormatter)}
            时区：$DEFAULT_TIMEZONE
            当前星期：${anchors.currentWeekday}
            本周周一：${anchors.currentWeekMonday}
            上周周一：${anchors.previousWeekMonday}
            下周周一：${anchors.nextWeekMonday}
            用户语句：$text
            请按规则输出 JSON。
        """.trimIndent()
    }

    private fun buildDateAnchors(now: LocalDateTime): DateAnchors {
        val currentDate = now.toLocalDate()
        val currentWeekMonday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return DateAnchors(
            currentWeekday = formatChineseWeekday(currentDate.dayOfWeek),
            currentWeekMonday = currentWeekMonday.toString(),
            previousWeekMonday = currentWeekMonday.minusWeeks(1).toString(),
            nextWeekMonday = currentWeekMonday.plusWeeks(1).toString()
        )
    }

    private fun formatChineseWeekday(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }

    private fun stripCodeFence(content: String): String {
        return content.trim()
            .replace(Regex("^```(?:json|JSON)?\\s*"), "")
            .replace(Regex("\\s*```$"), "")
            .trim()
    }

    private data class DateAnchors(
        val currentWeekday: String,
        val currentWeekMonday: String,
        val previousWeekMonday: String,
        val nextWeekMonday: String
    )

    companion object {
        private const val DEFAULT_TIMEZONE = "Asia/Shanghai"
        private const val DATE_TEXT_PATTERN = "yyyy-MM-dd HH:mm:ss"
    }
}
