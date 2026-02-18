package com.example.voicebill.ui.screens.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账记录") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("搜索记录...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(uiState.transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction) },
                            onClick = { viewModel.startEditing(transaction) }
                        )
                    }
                }
            }
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
        LaunchedEffect(error) {
            // Show snackbar or toast
        }
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
fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${transaction.amountCents / 100.0}",
                color = if (transaction.type == TransactionType.EXPENSE)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        },
        supportingContent = {
            Column {
                Text(transaction.categoryNameSnapshot)
                Text(
                    transaction.note ?: transaction.rawText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    editAmount: Long,
    editCategoryId: Long?,
    editType: TransactionType,
    editDate: Long,
    editNote: String,
    categories: List<Category>,
    onAmountChanged: (Long) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
    onDateSelected: (Long) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var amountText by remember {
        mutableStateOf(if (editAmount > 0) (editAmount / 100.0).toString() else "")
    }

    val filteredCategories = categories.filter {
        it.isIncome == (editType == TransactionType.INCOME) && !it.isDeleted
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("编辑记录") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 类型选择
                Text("类型", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = editType == TransactionType.EXPENSE,
                        onClick = { onTypeSelected(TransactionType.EXPENSE) },
                        label = { Text("支出") }
                    )
                    FilterChip(
                        selected = editType == TransactionType.INCOME,
                        onClick = { onTypeSelected(TransactionType.INCOME) },
                        label = { Text("收入") }
                    )
                }

                // 金额输入
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { text ->
                        amountText = text
                        val amount = text.toDoubleOrNull()
                        if (amount != null) {
                            onAmountChanged((amount * 100).toLong())
                        } else if (text.isEmpty()) {
                            onAmountChanged(0)
                        }
                    },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("¥") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 日期时间选择
                Text("日期时间", style = MaterialTheme.typography.labelMedium)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateFormat.format(Date(editDate)))
                }

                // 分类选择
                Text("分类", style = MaterialTheme.typography.labelMedium)
                if (filteredCategories.isEmpty()) {
                    Text(
                        "暂无可用分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredCategories) { category ->
                            FilterChip(
                                selected = editCategoryId == category.id,
                                onClick = { onCategorySelected(category.id) },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }

                // 备注输入
                OutlinedTextField(
                    value = editNote,
                    onValueChange = onNoteChanged,
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val oldCalendar = Calendar.getInstance().apply { timeInMillis = editDate }
                        val newCalendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        newCalendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                        newCalendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                        onDateSelected(newCalendar.timeInMillis)
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("下一步")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = editDate }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val newCalendar = Calendar.getInstance().apply { timeInMillis = editDate }
                    newCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    newCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    onDateSelected(newCalendar.timeInMillis)
                    showTimePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}
