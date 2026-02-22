package com.example.voicebill.data.repository

import com.example.voicebill.data.remote.ChatCompletionRequest
import com.example.voicebill.data.remote.ChatCompletionResponse
import com.example.voicebill.data.remote.ChatMessage
import com.example.voicebill.data.remote.Choice
import com.example.voicebill.data.remote.DeepSeekApi
import com.example.voicebill.di.SecurePrefs
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BillParserRepositoryImplTest {

    private lateinit var deepSeekApi: DeepSeekApi
    private lateinit var securePrefs: SecurePrefs
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var repository: BillParserRepositoryImpl

    private val fixedClock = Clock.fixed(
        Instant.parse("2026-02-22T07:30:00Z"),
        ZoneId.of("Asia/Shanghai")
    )

    @Before
    fun setup() {
        deepSeekApi = mockk()
        securePrefs = mockk()
        categoryRepository = mockk()

        repository = BillParserRepositoryImpl(
            deepSeekApi = deepSeekApi,
            securePrefs = securePrefs,
            categoryRepository = categoryRepository,
            clock = fixedClock
        )

        every { securePrefs.getApiKey() } returns "test-api-key"
        every { categoryRepository.getAllCategories() } returns flowOf(
            listOf(
                Category(name = "餐饮", icon = "", color = "", isIncome = false),
                Category(name = "交通", icon = "", color = "", isIncome = false),
                Category(name = "购物", icon = "", color = "", isIncome = false),
                Category(name = "工资", icon = "", color = "", isIncome = true),
                Category(name = "未分类", icon = "", color = "", isIncome = false, isUncategorized = true),
                Category(name = "未分类", icon = "", color = "", isIncome = true, isUncategorized = true)
            )
        )
    }

    @Test
    fun `parseBillText should merge local amount and api dateText`() = runTest {
        coEvery { deepSeekApi.createChatCompletion(any(), any()) } returns buildResponse(
            """
            {"amountCents":2100,"categoryName":"交通","type":"expense","dateText":"2026-02-23 08:00:00","timezone":"Asia/Shanghai","note":"开会打车"}
            """.trimIndent()
        )

        val result = repository.parseBillText("下周一早上开会打车20元")

        assertTrue(result.parseSuccess)
        // 本地金额优先，避免 AI 金额漂移
        assertEquals(2000, result.amountCents)
        assertEquals(
            Instant.parse("2026-02-23T00:00:00Z").toEpochMilli(),
            result.date
        )
    }

    @Test
    fun `parseBillText should fallback to current time when dateText is invalid`() = runTest {
        coEvery { deepSeekApi.createChatCompletion(any(), any()) } returns buildResponse(
            """
            {"amountCents":3200,"categoryName":"餐饮","type":"expense","dateText":"not-a-date","timezone":"Asia/Shanghai","note":"吃饭"}
            """.trimIndent()
        )

        val result = repository.parseBillText("今天吃饭32元")

        assertTrue(result.parseSuccess)
        assertEquals(fixedClock.instant().toEpochMilli(), result.date)
    }

    @Test
    fun `parseBillText should build prompt with anchored week context`() = runTest {
        val requestSlot = slot<ChatCompletionRequest>()
        coEvery { deepSeekApi.createChatCompletion(any(), capture(requestSlot)) } returns buildResponse(
            """
            {"amountCents":5300,"categoryName":"餐饮","type":"expense","dateText":"2026-02-11 19:00:00","timezone":"Asia/Shanghai","note":"烧烤"}
            """.trimIndent()
        )

        repository.parseBillText("上周三晚上烧烤53元")

        val messages = requestSlot.captured.messages
        assertEquals(2, messages.size)
        assertEquals("system", messages[0].role)
        assertEquals("user", messages[1].role)
        assertTrue(messages[0].content.contains("dateText"))
        assertTrue(messages[1].content.contains("当前星期：周日"))
        assertTrue(messages[1].content.contains("本周周一：2026-02-16"))
        assertTrue(messages[1].content.contains("上周周一：2026-02-09"))
        assertTrue(messages[1].content.contains("下周周一：2026-02-23"))
        assertTrue(messages[1].content.contains("用户语句：上周三晚上烧烤53元"))
    }

    @Test
    fun `parseBillText should parse json wrapped by code fence`() = runTest {
        coEvery { deepSeekApi.createChatCompletion(any(), any()) } returns buildResponse(
            """
            ```json
            {"amountCents":1800,"categoryName":"餐饮","type":"expense","dateText":"2026-02-21 08:00:00","timezone":"Asia/Shanghai","note":"咖啡"}
            ```
            """.trimIndent()
        )

        val result = repository.parseBillText("昨天早上咖啡18元")

        assertTrue(result.parseSuccess)
        assertEquals(
            Instant.parse("2026-02-21T00:00:00Z").toEpochMilli(),
            result.date
        )
    }

    @Test
    fun `parseBillText should return failure when api throws`() = runTest {
        coEvery { deepSeekApi.createChatCompletion(any(), any()) } throws RuntimeException("network down")

        val result = repository.parseBillText("今天吃饭32元")

        assertFalse(result.parseSuccess)
        assertTrue(result.errorMessage?.contains("network down") == true)
    }

    private fun buildResponse(content: String): ChatCompletionResponse {
        return ChatCompletionResponse(
            id = "test-id",
            objectType = "chat.completion",
            created = 0L,
            model = "deepseek-chat",
            choices = listOf(
                Choice(
                    index = 0,
                    message = ChatMessage(role = "assistant", content = content),
                    finishReason = "stop"
                )
            ),
            usage = null
        )
    }
}
