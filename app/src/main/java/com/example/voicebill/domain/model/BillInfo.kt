package com.example.voicebill.domain.model

/**
 * 解析后的记账信息
 */
data class BillInfo(
    val amountCents: Long?,           // 金额（分），null 表示无法解析
    val categoryName: String?,         // 分类名称
    val type: TransactionType?,       // 收入/支出类型
    val date: Long?,                   // 交易时间（毫秒时间戳）
    val note: String? = null,          // 备注
    val parseSuccess: Boolean = true, // 解析是否成功
    val errorMessage: String? = null  // 错误信息
)
