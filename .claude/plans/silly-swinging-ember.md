# 记录页面编辑功能实现计划

## 目标
在 RecordsScreen 中添加编辑 Transaction 功能，支持修改所有属性：金额、分类、日期时间、备注、交易类型。

## 交互方式
使用弹窗 (AlertDialog) 方式，点击记录条目弹出编辑对话框。

## 需要修改的文件

### 1. RecordsViewModel.kt
路径：`app/src/main/java/com/example/voicebill/ui/screens/records/RecordsViewModel.kt`

修改内容：
- 注入 `CategoryRepository` 获取分类列表
- 扩展 `RecordsUiState` 添加编辑状态字段：
  - `categories: List<Category>` - 分类列表
  - `editAmount: Long` - 编辑中的金额
  - `editCategoryId: Long?` - 编辑中的分类ID
  - `editType: TransactionType` - 编辑中的类型
  - `editDate: Long` - 编辑中的日期
  - `editNote: String` - 编辑中的备注
- 添加方法：
  - `loadCategories()` - 加载分类列表
  - `startEditing(transaction)` - 开始编辑，填充状态
  - `onEditAmountChanged(amount)` - 金额变更
  - `onEditCategorySelected(categoryId)` - 分类选择
  - `onEditTypeSelected(type)` - 类型切换（需清空不匹配的分类）
  - `onEditDateSelected(date)` - 日期选择
  - `onEditNoteChanged(note)` - 备注变更
  - `saveEditedTransaction()` - 保存编辑
  - `cancelEditing()` - 取消编辑

### 2. RecordsScreen.kt
路径：`app/src/main/java/com/example/voicebill/ui/screens/records/RecordsScreen.kt`

修改内容：
- 修改 `TransactionItem`：添加 `onClick` 参数，使用 `Modifier.clickable`
- 新增 `EditTransactionDialog` 组件，包含：
  - 类型选择 (FilterChip: 支出/收入)
  - 金额输入 (OutlinedTextField)
  - 日期时间显示与选择按钮 (DatePickerDialog + TimePickerDialog)
  - 分类选择 (FilterChip + LazyRow，根据类型过滤)
  - 备注输入 (OutlinedTextField)
  - 保存/取消按钮
- 在 `RecordsScreen` 中集成编辑对话框

## 实现细节

### 数据流
```
点击记录 → startEditing() → 显示 EditTransactionDialog
    ↓
修改字段 → onEdit*Changed() → 更新 UiState
    ↓
点击保存 → saveEditedTransaction() → updateTransaction() → 列表自动刷新
```

### 保存前校验规则
1. 金额必须 > 0，否则提示"请输入有效金额"
2. 必须选择分类（editCategoryId 不能为 null），否则提示"请选择分类"
3. 保存时根据 editCategoryId 重新查询分类，更新 categoryNameSnapshot

### 类型切换联动
当用户切换收入/支出类型时，检查当前选中分类是否匹配新类型，不匹配则清空 `editCategoryId`。

### 保留字段
编辑时保留原始 `id`、`rawText`、`createdAt` 不变。

### UI 滚动处理
EditTransactionDialog 内容区使用 `Column(modifier = Modifier.verticalScroll(rememberScrollState()))` 包裹，确保小屏设备可滚动查看所有字段。

### 分类订阅生命周期
在 ViewModel init 中调用 loadCategories()，使用单一 collect 订阅，避免多重订阅问题。

## 验证方式
1. 构建 APK：`gradlew.bat assembleDebug`
2. 安装到设备测试：
   - 点击记录条目，确认弹出编辑对话框
   - 修改各字段（金额、分类、日期、备注、类型）
   - 保存后确认列表更新
   - 切换类型时确认分类正确过滤并清空不匹配的分类选择
   - 测试空金额/未选分类时的错误提示
   - 验证保存后 categoryNameSnapshot 正确更新
