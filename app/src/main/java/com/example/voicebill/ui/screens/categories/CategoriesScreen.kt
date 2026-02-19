package com.example.voicebill.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voicebill.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddDialog
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加分类")
            }
        }
    ) { paddingValues ->
        val expenseCategories = uiState.categories.filter { !it.isIncome }
        val incomeCategories = uiState.categories.filter { it.isIncome }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 支出分类标题
            item {
                Text(
                    "支出分类",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 支出分类列表
            items(expenseCategories, key = { it.id }) { category ->
                CategoryItem(
                    category = category,
                    onEdit = { viewModel.showEditDialog(category) },
                    onDelete = { viewModel.deleteCategory(category.id) }
                )
            }

            // 分隔线
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 收入分类标题
            item {
                Text(
                    "收入分类",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 收入分类列表
            items(incomeCategories, key = { it.id }) { category ->
                CategoryItem(
                    category = category,
                    onEdit = { viewModel.showEditDialog(category) },
                    onDelete = { viewModel.deleteCategory(category.id) }
                )
            }
        }

        // 添加/编辑对话框
        if (uiState.showAddDialog) {
            CategoryDialog(
                category = uiState.editingCategory,
                onDismiss = viewModel::dismissDialog,
                onConfirm = { name, icon, color, isIncome ->
                    if (uiState.editingCategory != null) {
                        viewModel.updateCategory(uiState.editingCategory!!.id, name, icon, color)
                    } else {
                        viewModel.addCategory(name, icon, color, isIncome)
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(category.name) },
        supportingContent = {
            when {
                category.isUncategorized -> Text("系统分类（不可删除）")
                category.isDefault -> Text("默认分类")
                else -> null
            }
        },
        leadingContent = {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Row {
                if (!category.isUncategorized) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除分类 \"${category.name}\" 吗？\n\n该分类下的所有交易记录将被移动到\"未分类\"。") },
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
fun CategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String, isIncome: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var isIncome by remember { mutableStateOf(category?.isIncome ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "添加分类" else "编辑分类") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    singleLine = true
                )

                if (category == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("类型:")
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !isIncome,
                            onClick = { isIncome = false },
                            label = { Text("支出") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = isIncome,
                            onClick = { isIncome = true },
                            label = { Text("收入") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, "category", "#000000", isIncome) },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
