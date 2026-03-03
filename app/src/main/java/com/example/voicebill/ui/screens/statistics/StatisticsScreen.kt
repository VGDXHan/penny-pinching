package com.example.voicebill.ui.screens.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicebill.domain.model.CategorySummary
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.ui.components.PieChartCard
import com.example.voicebill.ui.screens.records.EditTransactionDialog
import com.example.voicebill.ui.screens.records.TransactionItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val periodOptions = remember {
        listOf(
            StatisticsPeriod.DAILY,
            StatisticsPeriod.WEEKLY,
            StatisticsPeriod.MONTHLY,
            StatisticsPeriod.YEARLY,
            StatisticsPeriod.CUSTOM
        )
    }

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periodOptions.forEach { period ->
                        FilterChip(
                            selected = uiState.selectedPeriod == period,
                            onClick = {
                                if (period == StatisticsPeriod.CUSTOM) {
                                    viewModel.onCustomRangeClick()
                                } else {
                                    viewModel.onPeriodSelected(period)
                                }
                            },
                            label = { Text(period.toDisplayText()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                uiState.statistics?.let { stats ->
                    item {
                        StatisticsRangeNavigationCard(
                            rangeText = formatDisplayRange(stats.startDate, stats.endDate),
                            canNavigatePrevious = uiState.selectedPeriod != StatisticsPeriod.CUSTOM,
                            canNavigateNext = uiState.selectedPeriod != StatisticsPeriod.CUSTOM && uiState.periodOffset > 0,
                            isCustom = uiState.selectedPeriod == StatisticsPeriod.CUSTOM,
                            onPreviousClick = { viewModel.onPreviousPeriod() },
                            onNextClick = { viewModel.onNextPeriod() },
                            onSelectCustomRange = { viewModel.onCustomRangeClick() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "收入",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "¥${stats.totalIncome / 100.0}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "支出",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "¥${stats.totalExpense / 100.0}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("结余")
                                    Text(
                                        "¥${stats.balance / 100.0}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (stats.balance >= 0)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (stats.expenseCategorySummaries.isNotEmpty()) {
                        item {
                            PieChartCard(
                                title = "支出分类",
                                data = stats.expenseCategorySummaries,
                                selectedCategoryId = uiState.selectedExpenseCategoryId,
                                onCategoryClick = { categoryId ->
                                    viewModel.onExpenseCategorySelected(categoryId)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(
                            items = stats.expenseCategorySummaries,
                            key = { summary -> "expense-${summary.categoryId}" }
                        ) { summary ->
                            Column {
                                CategorySummaryItem(
                                    summary = summary,
                                    isSelected = summary.categoryId == uiState.selectedExpenseCategoryId,
                                    onClick = {
                                        viewModel.onExpenseCategorySelected(summary.categoryId)
                                    }
                                )
                                if (summary.categoryId == uiState.selectedExpenseCategoryId) {
                                    CategoryTransactionDetailSection(
                                        transactions = uiState.expenseCategoryTransactions,
                                        isLoading = uiState.isExpenseDetailLoading,
                                        onTransactionClick = viewModel::startEditing,
                                        onDeleteTransaction = viewModel::deleteTransaction
                                    )
                                }
                            }
                        }
                    }

                    if (stats.incomeCategorySummaries.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            PieChartCard(
                                title = "收入分类",
                                data = stats.incomeCategorySummaries,
                                selectedCategoryId = uiState.selectedIncomeCategoryId,
                                onCategoryClick = { categoryId ->
                                    viewModel.onIncomeCategorySelected(categoryId)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(
                            items = stats.incomeCategorySummaries,
                            key = { summary -> "income-${summary.categoryId}" }
                        ) { summary ->
                            Column {
                                CategorySummaryItem(
                                    summary = summary,
                                    isSelected = summary.categoryId == uiState.selectedIncomeCategoryId,
                                    onClick = {
                                        viewModel.onIncomeCategorySelected(summary.categoryId)
                                    }
                                )
                                if (summary.categoryId == uiState.selectedIncomeCategoryId) {
                                    CategoryTransactionDetailSection(
                                        transactions = uiState.incomeCategoryTransactions,
                                        isLoading = uiState.isIncomeDetailLoading,
                                        onTransactionClick = viewModel::startEditing,
                                        onDeleteTransaction = viewModel::deleteTransaction
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无数据")
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCustomRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = uiState.customRange?.startUtcDateMillis,
            initialSelectedEndDateMillis = uiState.customRange?.endUtcDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.onCustomRangeDismiss() },
            confirmButton = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                TextButton(
                    enabled = start != null && end != null,
                    onClick = {
                        viewModel.onCustomRangeConfirmed(
                            startUtcDateMillis = start!!,
                            endUtcDateMillis = end!!
                        )
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCustomRangeDismiss() }) {
                    Text("取消")
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState)
        }
    }

    if (uiState.editingTransaction != null) {
        EditTransactionDialog(
            editAmount = uiState.editAmount,
            editCategoryId = uiState.editCategoryId,
            editType = uiState.editType,
            editDate = uiState.editDate,
            editNote = uiState.editNote,
            categories = uiState.categories,
            onAmountChanged = viewModel::onEditAmountChanged,
            onCategorySelected = viewModel::onEditCategorySelected,
            onTypeSelected = viewModel::onEditTypeSelected,
            onDateSelected = viewModel::onEditDateSelected,
            onNoteChanged = viewModel::onEditNoteChanged,
            onSave = viewModel::saveEditedTransaction,
            onCancel = viewModel::cancelEditing
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("提示") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun StatisticsRangeNavigationCard(
    rangeText: String,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    isCustom: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSelectCustomRange: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = canNavigatePrevious,
                    onClick = onPreviousClick
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "上一段")
                }

                Text(
                    text = rangeText,
                    style = MaterialTheme.typography.bodyLarge
                )

                IconButton(
                    enabled = canNavigateNext,
                    onClick = onNextClick
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "下一段")
                }
            }

            if (isCustom) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = onSelectCustomRange) {
                        Text("重新选择范围")
                    }
                }
            }
        }
    }
}

private fun StatisticsPeriod.toDisplayText(): String {
    return when (this) {
        StatisticsPeriod.DAILY -> "今日"
        StatisticsPeriod.WEEKLY -> "本周"
        StatisticsPeriod.MONTHLY -> "本月"
        StatisticsPeriod.YEARLY -> "今年"
        StatisticsPeriod.CUSTOM -> "自定义"
    }
}

private fun formatDisplayRange(startDate: Long, endDate: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(startDate).atZone(zone).toLocalDate()
    val endInclusive = Instant.ofEpochMilli(endDate).atZone(zone).toLocalDate().minusDays(1)
    return "${start.format(formatter)} ~ ${endInclusive.format(formatter)}"
}

@Composable
fun CategorySummaryItem(
    summary: CategorySummary,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val safeProgress = summary.percentage
                .takeIf { it.isFinite() }
                ?.coerceIn(0f, 1f)
                ?: 0f
            Column {
                Text(
                    summary.categoryName,
                    style = MaterialTheme.typography.bodyLarge
                )
                LinearProgressIndicator(
                    progress = { safeProgress },
                    modifier = Modifier
                        .width(100.dp)
                        .padding(top = 4.dp),
                )
            }
            Text(
                "¥${String.format("%.2f", summary.amountCents / 100.0)}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun CategoryTransactionDetailSection(
    transactions: List<Transaction>,
    isLoading: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            transactions.isEmpty() -> {
                Text(
                    text = "当前周期该分类暂无记录",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            else -> {
                Column {
                    transactions.forEachIndexed { index, transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { onDeleteTransaction(transaction) },
                            onClick = { onTransactionClick(transaction) }
                        )
                        if (index < transactions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
