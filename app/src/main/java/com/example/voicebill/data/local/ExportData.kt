package com.example.voicebill.data.local

import com.example.voicebill.data.local.entity.CategoryEntity
import com.example.voicebill.data.local.entity.TransactionEntity
import com.google.gson.annotations.SerializedName

/**
 * 导出数据结构
 */
data class ExportData(
    @SerializedName("schemaVersion")
    val schemaVersion: Int = 1,

    @SerializedName("exportedAt")
    val exportedAt: Long = System.currentTimeMillis(),

    @SerializedName("appVersion")
    val appVersion: String = "1.0.0",

    @SerializedName("categories")
    val categories: List<CategoryExport>,

    @SerializedName("transactions")
    val transactions: List<TransactionExport>
)

data class CategoryExport(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("color")
    val color: String,
    @SerializedName("isIncome")
    val isIncome: Boolean,
    @SerializedName("isDefault")
    val isDefault: Boolean,
    @SerializedName("isDeleted")
    val isDeleted: Boolean
)

data class TransactionExport(
    @SerializedName("id")
    val id: Long,
    @SerializedName("amountCents")
    val amountCents: Long,
    @SerializedName("categoryId")
    val categoryId: Long?,
    @SerializedName("categoryNameSnapshot")
    val categoryNameSnapshot: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("date")
    val date: Long,
    @SerializedName("note")
    val note: String?,
    @SerializedName("rawText")
    val rawText: String,
    @SerializedName("createdAt")
    val createdAt: Long
)

// Extension functions to convert between entity and export models
fun CategoryEntity.toExport(): CategoryExport = CategoryExport(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isIncome = isIncome,
    isDefault = isDefault,
    isDeleted = isDeleted
)

fun CategoryExport.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isIncome = isIncome,
    isDefault = isDefault,
    isDeleted = isDeleted
)

fun TransactionEntity.toExport(): TransactionExport = TransactionExport(
    id = id,
    amountCents = amountCents,
    categoryId = categoryId,
    categoryNameSnapshot = categoryNameSnapshot,
    type = type,
    date = date,
    note = note,
    rawText = rawText,
    createdAt = createdAt
)

fun TransactionExport.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amountCents = amountCents,
    categoryId = categoryId,
    categoryNameSnapshot = categoryNameSnapshot,
    type = type,
    date = date,
    note = note,
    rawText = rawText,
    createdAt = createdAt
)
