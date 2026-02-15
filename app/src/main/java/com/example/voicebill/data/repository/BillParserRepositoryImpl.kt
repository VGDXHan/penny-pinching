package com.example.voicebill.data.repository

import com.example.voicebill.data.remote.ChatCompletionRequest
import com.example.voicebill.data.remote.ChatMessage
import com.example.voicebill.data.remote.DeepSeekApi
import com.example.voicebill.di.SecurePrefs
import com.example.voicebill.domain.model.BillInfo
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.BillParserRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillParserRepositoryImpl @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val securePrefs: SecurePrefs,
    private val clock: Clock = Clock.systemDefaultZone()
) : BillParserRepository {

    private val gson = Gson()

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
            // 首先尝试本地解析
            val localResult = parseLocally(text)
            if (localResult.parseSuccess && localResult.amountCents != null) {
                return localResult
            }

            // 本地解析失败，尝试 API 解析
            return parseWithApi(text, apiKey)
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

        // 解析时间
        val date = parseDate(text)

        // 解析备注（去除金额和分类相关词汇后的剩余部分）
        val note = parseNote(text)

        return BillInfo(
            amountCents = amountCents,
            categoryName = categoryName,
            type = type,
            date = date,
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
                return (amount * 100).toLong()
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

    private fun parseDate(text: String): Long {
        val now = LocalDate.now(clock.withZone(ZoneId.systemDefault()))
        val date = when {
            text.contains("今天") -> now
            text.contains("昨天") -> now.minusDays(1)
            text.contains("前天") -> now.minusDays(2)
            text.contains("明天") -> now.plusDays(1)
            text.contains("后天") -> now.plusDays(2)
            text.contains("上周") -> {
                val dayOfWeek = parseDayOfWeek(text)
                if (dayOfWeek != null) {
                    now.minusWeeks(1).with(dayOfWeek)
                } else now.minusWeeks(1)
            }
            text.contains("下周") -> {
                val dayOfWeek = parseDayOfWeek(text)
                if (dayOfWeek != null) {
                    now.plusWeeks(1).with(dayOfWeek)
                } else now.plusWeeks(1)
            }
            else -> {
                val dayOfWeek = parseDayOfWeek(text)
                dayOfWeek?.let { now.with(it) } ?: now
            }
        }

        // 设置为当天 12:00:00
        return date.atTime(LocalTime.NOON)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun parseDayOfWeek(text: String): java.time.DayOfWeek? {
        return when {
            text.contains("周一") || text.contains("星期一") -> java.time.DayOfWeek.MONDAY
            text.contains("周二") || text.contains("星期二") -> java.time.DayOfWeek.TUESDAY
            text.contains("周三") || text.contains("星期三") -> java.time.DayOfWeek.WEDNESDAY
            text.contains("周四") || text.contains("星期四") -> java.time.DayOfWeek.THURSDAY
            text.contains("周五") || text.contains("星期五") -> java.time.DayOfWeek.FRIDAY
            text.contains("周六") || text.contains("星期六") -> java.time.DayOfWeek.SATURDAY
            text.contains("周日") || text.contains("星期日") || text.contains("星期天") -> java.time.DayOfWeek.SUNDAY
            else -> null
        }
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
        val currentTime = LocalDateTime.now(clock)
        val prompt = """
            你是一个记账助手，请从用户的记账语句中提取信息。

            重要参考信息：当前时间是 ${currentTime.year}年${currentTime.monthValue}月${currentTime.dayOfMonth}日 ${currentTime.hour}时${currentTime.minute}分

            用户的记账语句：$text

            请从以下记账语句中提取：
            1. 金额（单位：分，即元的100倍）
            2. 分类（只能是以下之一：餐饮、交通、购物、工资、红包、其他）
            3. 类型（income 表示收入，expense 表示支出）
            4. 时间（支持：今天、昨天、上周三、下周五等自然语言，请转换为毫秒时间戳）
            5. 备注（可选，从语句中提取）

            请严格按照以下JSON格式输出，不要输出任何解释文字：
            {"amountCents": 金额(整数分), "categoryName": "分类名", "type": "income"或"expense", "date": 时间戳(毫秒), "note": "备注(可选)"}

            注意：
            - 如果无法确定金额，amountCents 设为 null
            - 如果是收入（工资、奖金、红包等），type 为 "income"
            - 如果是支出（吃饭、购物、加油等），type 为 "expense"
            - 时间以本地时区解析，"上周"以周一为起点
            - 只输出JSON，不要输出任何其他内容
        """.trimIndent()

        val request = ChatCompletionRequest(
            model = "deepseek-chat",
            messages = listOf(
                ChatMessage(role = "user", content = prompt)
            ),
            temperature = 0.1,
            maxTokens = 500
        )

        val response = deepSeekApi.createChatCompletion(
            authorization = "Bearer $apiKey",
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
            val json = gson.fromJson(content, Map::class.java)

            val amountCents = (json["amountCents"] as? Number)?.toLong()
            val categoryName = json["categoryName"] as? String
            val typeStr = json["type"] as? String
            val dateTimestamp = (json["date"] as? Number)?.toLong()
            val note = json["note"] as? String

            val type = when (typeStr) {
                "income" -> TransactionType.INCOME
                "expense" -> TransactionType.EXPENSE
                else -> null
            }

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
        val matcher = jsonPattern.matcher(content)

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
}
