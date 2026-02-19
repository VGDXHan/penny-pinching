# 20260218 添加日期时间选择功能

## 需求描述

在记账页面的"解析结果"区域添加日期时间选择器，允许用户手动调整 DeepSeek 解析出的日期和时间（精确到分钟）。

## 实现方案

### 修改文件

1. `app/src/main/java/com/example/voicebill/ui/screens/home/HomeViewModel.kt`
2. `app/src/main/java/com/example/voicebill/ui/screens/home/HomeScreen.kt`
3. `app/src/main/java/com/example/voicebill/di/ApiModule.kt`

### HomeViewModel.kt 修改

1. 在 `HomeUiState` 中添加 `selectedDate: Long` 字段（默认当前时间）
2. 解析成功后将 `parseResult.date` 同步到 `selectedDate`
3. 添加 `onDateSelected(date: Long)` 方法
4. `saveTransaction()` 中使用 `state.selectedDate` 而非 `parseResult?.date`

```kotlin
data class HomeUiState(
    // ... 其他字段
    val selectedDate: Long = System.currentTimeMillis(),
    // ...
)

fun onDateSelected(date: Long) {
    _uiState.value = _uiState.value.copy(selectedDate = date)
}
```

### HomeScreen.kt 修改

1. 添加滚动支持（解决内容被底部导航栏遮挡问题）
2. 在金额和分类之间添加日期时间选择行
3. 使用 Material 3 的 `DatePickerDialog` 和 `TimePickerDialog`

```kotlin
// 添加滚动
.verticalScroll(rememberScrollState())

// 日期时间显示
Row {
    Text("时间: ")
    Text(SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(uiState.selectedDate)))
    IconButton(onClick = { showDatePicker = true }) {
        Icon(Icons.Default.Edit, "编辑时间")
    }
}
```

### UI 流程

点击时间 → 弹出日期选择器 → 点击"下一步" → 弹出时间选择器 → 点击"确定" → 完成

## Bug 修复

### API Key 换行符问题

**问题**：粘贴 API Key 时可能带入换行符（0x0a），导致请求失败，报错 `Unexpected char 0x0a at 74 in Authorization value`

**解决**：在 `SecurePrefs` 中保存和读取时自动 trim

```kotlin
fun saveApiKey(key: String) {
    prefs.edit().putString(ApiConstants.KEY_API_KEY, key.trim()).apply()
}

fun getApiKey(): String? = prefs.getString(ApiConstants.KEY_API_KEY, null)?.trim()?.ifEmpty { null }
```

### 保存按钮被遮挡问题

**问题**：解析结果卡片内容较多时，保存按钮被底部导航栏遮挡

**解决**：给 Column 添加 `verticalScroll(rememberScrollState())`

## 技术要点

1. Material 3 日期选择器使用 `rememberDatePickerState()` 和 `DatePickerDialog`
2. Material 3 时间选择器使用 `rememberTimePickerState()` 和 `TimePicker`（放在 AlertDialog 中）
3. 日期时间存储为毫秒时间戳（Long），与现有 `BillInfo.date` 一致
4. 使用 `Calendar` 类合并日期和时间

---

## 显示解析时间功能

### 需求

点击解析按钮后，在记账界面显示当前时间（解析时的时间），方便用户了解解析发生的时刻。

### 实现方案

在 ViewModel 层记录解析时间，存入 uiState，UI 层显示。

### 修改内容

#### HomeViewModel.kt

1. `HomeUiState` 添加 `parseTime: Long?` 字段
2. `parseText()` 在调用 API 前记录 `System.currentTimeMillis()`
3. 解析成功后将 `parseTime` 存入 uiState

```kotlin
data class HomeUiState(
    // ... 其他字段
    val parseTime: Long? = null
)

fun parseText() {
    val parseTime = System.currentTimeMillis()  // 记录解析时间

    viewModelScope.launch {
        // ... 解析逻辑
        _uiState.value = _uiState.value.copy(
            // ... 其他字段
            parseTime = parseTime
        )
    }
}
```

#### HomeScreen.kt

在"解析结果"标题下方显示解析时间：

```kotlin
Text("解析结果", style = MaterialTheme.typography.titleMedium)
uiState.parseTime?.let { time ->
    Text(
        "解析于 ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time))}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
```

### 设计决策

选择在 ViewModel 层记录时间而非修改 BillInfo：
- 简单，不需要修改 Repository 和领域模型
- 时间用于 UI 展示，不需要持久化

---

## 记录页面编辑功能

### 需求

在 RecordsScreen 中添加编辑 Transaction 功能，支持修改所有属性：金额、分类、日期时间、备注、交易类型。

### 实现方案

#### RecordsViewModel.kt

1. 注入 `CategoryRepository` 获取分类列表
2. 扩展 `RecordsUiState` 添加编辑状态字段
3. 添加编辑相关方法：`startEditing`、`saveEditedTransaction`、`cancelEditing` 等

#### RecordsScreen.kt

1. `TransactionItem` 添加 `onClick` 参数，点击记录弹出编辑对话框
2. 新增 `EditTransactionDialog` 组件，包含类型选择、金额输入、日期时间选择、分类选择、备注输入

### Bug 修复

#### 金额输入被重写问题

**问题**：`amountText` 用 `remember(editAmount)` 初始化，每次输入都会触发状态更新导致文本被重写

**解决**：移除 `remember` 的 key 参数

#### 已删除分类名称丢失问题

**问题**：保存时从活跃分类查询 `categoryNameSnapshot`，已软删除的分类会导致名称变成空字符串

**解决**：查询失败时保留原始的 `categoryNameSnapshot`

```kotlin
val categoryName = category?.name
    ?: state.categories.find { it.id == state.editCategoryId }?.name
    ?: editingTransaction.categoryNameSnapshot
```

#### 金额浮点数精度问题

**问题**：`(amount * 100).toLong()` 会导致精度丢失，如 `0.29 * 100 = 28.999...` 截断为 `28`

**解决**：使用 `Math.round(amount * 100)` 进行四舍五入

**影响文件**：
- `RecordsScreen.kt`
- `HomeScreen.kt`
- `BillParserRepositoryImpl.kt`
