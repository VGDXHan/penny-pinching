package com.example.voicebill.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualTransactionDialog(
    createAmount: Long,
    createCategoryId: Long?,
    createType: TransactionType,
    createDate: Long,
    createNote: String,
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
        mutableStateOf(if (createAmount > 0) (createAmount / 100.0).toString() else "")
    }

    // 手动新增时分类根据收入/支出类型过滤。
    val filteredCategories = categories.filter {
        it.isIncome == (createType == TransactionType.INCOME) && !it.isDeleted
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("手动记账") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("类型", style = MaterialTheme.typography.labelMedium)
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = createType == TransactionType.EXPENSE,
                        onClick = { onTypeSelected(TransactionType.EXPENSE) },
                        label = { Text("支出") }
                    )
                    FilterChip(
                        selected = createType == TransactionType.INCOME,
                        onClick = { onTypeSelected(TransactionType.INCOME) },
                        label = { Text("收入") }
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { text ->
                        amountText = text
                        val amount = text.toDoubleOrNull()
                        if (amount != null) {
                            onAmountChanged(Math.round(amount * 100))
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

                Text("日期时间", style = MaterialTheme.typography.labelMedium)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateFormat.format(Date(createDate)))
                }

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
                                selected = createCategoryId == category.id,
                                onClick = { onCategorySelected(category.id) },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = createNote,
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = createDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val oldCalendar = Calendar.getInstance().apply { timeInMillis = createDate }
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

    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = createDate }
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
                    val newCalendar = Calendar.getInstance().apply { timeInMillis = createDate }
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
