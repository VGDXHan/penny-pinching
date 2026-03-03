package com.example.voicebill.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.data.local.entity.CategoryEntity
import com.example.voicebill.data.local.entity.TransactionEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ExportImportUseCaseTest {

    private val context: Context = mockk()
    private val contentResolver: ContentResolver = mockk()
    private val categoryDao: CategoryDao = mockk()
    private val transactionDao: TransactionDao = mockk()
    private val uri: Uri = mockk()

    init {
        every { context.contentResolver } returns contentResolver
    }

    @Test
    fun exportData_whenFlowLongLived_shouldReturnAndWriteNonEmptyJson() = runTest {
        val category = CategoryEntity(
            id = 1L,
            name = "餐饮",
            icon = "restaurant",
            color = "#FF9800",
            isIncome = false
        )
        val transaction = TransactionEntity(
            id = 10L,
            amountCents = 4580L,
            categoryId = 1L,
            categoryNameSnapshot = "餐饮",
            type = "EXPENSE",
            date = 1_700_000_000_000L,
            note = "早餐",
            rawText = "早餐 45.8",
            createdAt = 1_700_000_000_100L
        )

        every { categoryDao.getAllCategories() } returns flow {
            emit(listOf(category))
            awaitCancellation()
        }
        every { transactionDao.getAllTransactions() } returns flow {
            emit(listOf(transaction))
            awaitCancellation()
        }
        val output = ByteArrayOutputStream()
        every { contentResolver.openOutputStream(uri) } returns output

        val useCase = ExportImportUseCase(context, transactionDao, categoryDao)

        val result = withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(1_000) {
                useCase.exportData(uri)
            }
        }

        assertTrue(result is DataOperationResult.Success)
        assertTrue(output.size() > 0)
        val json = output.toString(Charsets.UTF_8.name())
        assertTrue(json.contains("\"categories\""))
        assertTrue(json.contains("\"transactions\""))
    }

    @Test
    fun exportData_whenOutputStreamNull_shouldReturnError() = runTest {
        every { categoryDao.getAllCategories() } returns flowOf(emptyList<CategoryEntity>())
        every { transactionDao.getAllTransactions() } returns flowOf(emptyList<TransactionEntity>())
        every { contentResolver.openOutputStream(uri) } returns null

        val useCase = ExportImportUseCase(context, transactionDao, categoryDao)

        val result = useCase.exportData(uri)

        assertTrue(result is DataOperationResult.Error)
        assertEquals("无法打开文件", (result as DataOperationResult.Error).error)
    }

    @Test
    fun exportData_whenDaoThrows_shouldReturnError() = runTest {
        every { categoryDao.getAllCategories() } throws IllegalStateException("db boom")

        val useCase = ExportImportUseCase(context, transactionDao, categoryDao)

        val result = useCase.exportData(uri)

        assertTrue(result is DataOperationResult.Error)
        assertEquals("导出失败: db boom", (result as DataOperationResult.Error).error)
    }
}

