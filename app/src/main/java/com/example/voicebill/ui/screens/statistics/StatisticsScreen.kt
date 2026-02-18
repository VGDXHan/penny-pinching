package com.example.voicebill.ui.screens.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import com.example.voicebill.domain.model.CategorySummary
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.ui.components.PieChartCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 页面重新显示时刷新数据
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 周期选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticsPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = uiState.selectedPeriod == period,
                        onClick = { viewModel.onPeriodSelected(period) },
                        label = {
                            Text(
                                when (period) {
                                    StatisticsPeriod.DAILY -> "今日"
                                    StatisticsPeriod.WEEKLY -> "本周"
                                    StatisticsPeriod.MONTHLY -> "本月"
                                    StatisticsPeriod.YEARLY -> "今年"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                uiState.statistics?.let { stats ->
                    // 总收入/支出
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

                    // 支出分类饼图
                    if (stats.expenseCategorySummaries.isNotEmpty()) {
                        PieChartCard(
                            title = "支出分类",
                            data = stats.expenseCategorySummaries,
                            selectedCategoryId = uiState.selectedExpenseCategoryId,
                            onCategoryClick = { categoryId ->
                                viewModel.onExpenseCategorySelected(categoryId)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        stats.expenseCategorySummaries.forEach { summary ->
                            CategorySummaryItem(
                                summary = summary,
                                isSelected = summary.categoryId == uiState.selectedExpenseCategoryId,
                                onClick = {
                                    viewModel.onExpenseCategorySelected(summary.categoryId)
                                }
                            )
                        }
                    }

                    // 收入分类饼图
                    if (stats.incomeCategorySummaries.isNotEmpty()) {
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

                        stats.incomeCategorySummaries.forEach { summary ->
                            CategorySummaryItem(
                                summary = summary,
                                isSelected = summary.categoryId == uiState.selectedIncomeCategoryId,
                                onClick = {
                                    viewModel.onIncomeCategorySelected(summary.categoryId)
                                }
                            )
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无数据")
                    }
                }
            }
        }
    }
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
            Column {
                Text(
                    summary.categoryName,
                    style = MaterialTheme.typography.bodyLarge
                )
                LinearProgressIndicator(
                    progress = { summary.percentage },
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
