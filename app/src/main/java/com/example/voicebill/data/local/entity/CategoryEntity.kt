package com.example.voicebill.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.voicebill.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val isIncome: Boolean,
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isIncome = isIncome,
        isDefault = isDefault,
        isDeleted = isDeleted
    )

    companion object {
        fun fromDomain(category: Category): CategoryEntity = CategoryEntity(
            id = category.id,
            name = category.name,
            icon = category.icon,
            color = category.color,
            isIncome = category.isIncome,
            isDefault = category.isDefault,
            isDeleted = category.isDeleted
        )
    }
}
