package com.example.voicebill.domain.model

data class Transaction(
    val id: Long = 0,
    val amountCents: Long,
    val categoryId: Long,
    val categoryNameSnapshot: String,
    val type: TransactionType,
    val date: Long,
    val note: String? = null,
    val rawText: String,
    val createdAt: Long = System.currentTimeMillis()
)
