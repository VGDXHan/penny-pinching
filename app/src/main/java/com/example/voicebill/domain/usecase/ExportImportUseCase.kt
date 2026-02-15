package com.example.voicebill.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.voicebill.data.local.ExportData
import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.data.local.toEntity
import com.example.voicebill.data.local.toExport
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

sealed class DataOperationResult {
    data class Success(val message: String) : DataOperationResult()
    data class Error(val error: String) : DataOperationResult()
}

class ExportImportUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun exportData(uri: Uri): DataOperationResult = withContext(Dispatchers.IO) {
        try {
            // 获取所有数据
            val categories = mutableListOf<com.example.voicebill.data.local.CategoryExport>()
            val transactions = mutableListOf<com.example.voicebill.data.local.TransactionExport>()

            // 收集分类数据
            categoryDao.getAllCategories().collect { categoryEntities ->
                categories.addAll(categoryEntities.map { it.toExport() })
            }

            // 收集交易数据
            transactionDao.getAllTransactions().collect { transactionEntities ->
                transactions.addAll(transactionEntities.map { it.toExport() })
            }

            // 创建导出数据
            val exportData = ExportData(
                categories = categories,
                transactions = transactions
            )

            // 写入文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(gson.toJson(exportData))
                }
            } ?: return@withContext DataOperationResult.Error("无法打开文件")

            DataOperationResult.Success("导出成功")
        } catch (e: Exception) {
            DataOperationResult.Error("导出失败: ${e.message}")
        }
    }

    suspend fun importData(uri: Uri): DataOperationResult = withContext(Dispatchers.IO) {
        try {
            // 读取文件
            val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext DataOperationResult.Error("无法读取文件")

            // 解析 JSON
            val exportData = try {
                gson.fromJson(jsonContent, ExportData::class.java)
            } catch (e: Exception) {
                return@withContext DataOperationResult.Error("JSON 格式错误: ${e.message}")
            }

            // 验证 schema version
            if (exportData.schemaVersion > 1) {
                return@withContext DataOperationResult.Error("不支持的数据版本: ${exportData.schemaVersion}")
            }

            // 清空现有数据
            transactionDao.deleteAllTransactions()
            categoryDao.deleteAllCategories()

            // 导入分类
            val categoryEntities = exportData.categories.map { it.toEntity() }
            categoryDao.insertCategories(categoryEntities)

            // 导入交易
            val transactionEntities = exportData.transactions.map { it.toEntity() }
            transactionDao.insertTransactions(transactionEntities)

            DataOperationResult.Success("导入成功")
        } catch (e: Exception) {
            DataOperationResult.Error("导入失败: ${e.message}")
        }
    }
}
