package com.example.voicebill.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["categoryId"]), Index(value = ["date"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountCents: Long,
    val categoryId: Long?,
    val categoryNameSnapshot: String,
    val type: String,
    val date: Long,
    val note: String? = null,
    val rawText: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        amountCents = amountCents,
        categoryId = categoryId ?: 0,
        categoryNameSnapshot = categoryNameSnapshot,
        type = TransactionType.valueOf(type),
        date = date,
        note = note,
        rawText = rawText,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity = TransactionEntity(
            id = transaction.id,
            amountCents = transaction.amountCents,
            categoryId = if (transaction.categoryId == 0L) null else transaction.categoryId,
            categoryNameSnapshot = transaction.categoryNameSnapshot,
            type = transaction.type.name,
            date = transaction.date,
            note = transaction.note,
            rawText = transaction.rawText,
            createdAt = transaction.createdAt
        )
    }
}
