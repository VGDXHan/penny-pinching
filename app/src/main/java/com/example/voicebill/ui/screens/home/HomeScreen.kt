package com.example.voicebill.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voicebill.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempSelectedDate by remember { mutableLongStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账") },
                actions = {
                    if (!uiState.hasApiKey) {
                        IconButton(onClick = { /* 导航到设置 */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 提示信息
            if (!uiState.hasApiKey) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "请先配置 DeepSeek API Key",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 记账输入框
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = viewModel::onInputTextChanged,
                label = { Text("输入记账内容") },
                placeholder = { Text("例如：今天中午吃火锅花了128元") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                trailingIcon = {
                    IconButton(
                        onClick = { /* 使用系统语音输入 */ }
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "语音输入")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::parseText,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.inputText.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("解析")
            }

            // 解析结果
            uiState.parseResult?.let { _ ->
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "解析结果",
                            style = MaterialTheme.typography.titleMedium
                        )
                        uiState.parseTime?.let { time ->
                            Text(
                                "解析于 ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // 类型选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.selectedType == TransactionType.EXPENSE,
                                onClick = { viewModel.onTypeSelected(TransactionType.EXPENSE) },
                                label = { Text("支出") },
                                leadingIcon = if (uiState.selectedType == TransactionType.EXPENSE) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = uiState.selectedType == TransactionType.INCOME,
                                onClick = { viewModel.onTypeSelected(TransactionType.INCOME) },
                                label = { Text("收入") },
                                leadingIcon = if (uiState.selectedType == TransactionType.INCOME) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 金额输入
                        OutlinedTextField(
                            value = (uiState.amount / 100.0).toString(),
                            onValueChange = { input ->
                                input.toDoubleOrNull()?.let {
                                    viewModel.onAmountChanged((it * 100).toLong())
                                }
                            },
                            label = { Text("金额") },
                            prefix = { Text("¥") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 日期时间选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("时间: ", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    .format(Date(uiState.selectedDate)),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.clickable {
                                    tempSelectedDate = uiState.selectedDate
                                    showDatePicker = true
                                }
                            )
                            IconButton(onClick = {
                                tempSelectedDate = uiState.selectedDate
                                showDatePicker = true
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "编辑时间",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 分类选择
                        Text("选择分类:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))

                        val filteredCategories = uiState.categories.filter {
                            it.isIncome == (uiState.selectedType == TransactionType.INCOME)
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredCategories) { category ->
                                FilterChip(
                                    selected = uiState.selectedCategoryId == category.id,
                                    onClick = { viewModel.onCategorySelected(category.id) },
                                    label = { Text(category.name) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 备注
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = viewModel::onNoteChanged,
                            label = { Text("备注") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = viewModel::saveTransaction,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text("保存")
                        }
                    }
                }
            }

            // 错误信息
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 成功信息
            uiState.successMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    message,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempSelectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                        val oldCalendar = Calendar.getInstance().apply { timeInMillis = tempSelectedDate }
                        val newCalendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                        newCalendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                        newCalendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                        tempSelectedDate = newCalendar.timeInMillis
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

    // 时间选择器
    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = tempSelectedDate }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val newCalendar = Calendar.getInstance().apply { timeInMillis = tempSelectedDate }
                    newCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    newCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    viewModel.onDateSelected(newCalendar.timeInMillis)
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
