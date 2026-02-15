package com.example.voicebill.domain.model

/**
 * 统计周期类型
 */
enum class StatisticsPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * 分类汇总数据
 */
data class CategorySummary(
    val categoryId: Long,
    val categoryName: String,
    val amountCents: Long,
    val percentage: Float
)

/**
 * 每日汇总数据（用于折线图）
 */
data class DailySummary(
    val date: Long,
    val incomeCents: Long,
    val expenseCents: Long
)

/**
 * 总体统计结果
 */
data class StatisticsResult(
    val period: StatisticsPeriod,
    val startDate: Long,
    val endDate: Long,
    val totalIncome: Long,
    val totalExpense: Long,
    val balance: Long,
    val incomeCategorySummaries: List<CategorySummary>,
    val expenseCategorySummaries: List<CategorySummary>,
    val dailySummaries: List<DailySummary>
)
