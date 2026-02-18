# 统计页面闪烁问题修复

## 问题描述

统计页面点击"今日/本周/本月"等筛选按钮时会出现屏幕闪烁刷新现象，从其他页面返回统计页面时也会闪烁，观感不好。

## 问题原因

1. **DisposableEffect 监听 ON_RESUME** - `StatisticsScreen.kt:30-42` 监听页面生命周期，每次页面恢复都调用 `refresh()` 并设置 `isLoading = true`，导致 UI 从内容切换到加载指示器再切回
2. **Period 切换时显示加载状态** - `StatisticsViewModel.kt:36-43` 的 `onPeriodSelected()` 调用 `loadStatistics()` 时设置 `isLoading = true`，导致同样的闪烁问题
3. **PieChart pointerInput key 问题** - `PieChart.kt:93` 的 `pointerInput` 只用 `data` 作为 key，当选中状态改变但数据没变时，会使用陈旧的 `selectedCategoryId` 导致点击行为不一致
4. **分类列表滚动问题** - 分类汇总列表使用 `forEach` 放在非滚动的 `Column` 中，当分类过多时超出屏幕无法滚动

## 解决方案

### 1. 移除不必要的页面恢复刷新

删除 `StatisticsScreen.kt` 中的 DisposableEffect 及相关导入：
- 移除 `LocalLifecycleOwner` 和 `DisposableEffect` 导入
- 删除监听 ON_RESUME 的代码块
- 统计页面首次进入时已在 ViewModel 的 `init` 中加载数据，不需要每次返回都刷新

### 2. 移除 Period 切换时的加载状态

修改 `StatisticsViewModel.kt` 的 `onPeriodSelected()` 方法：
- 不再调用 `loadStatistics()`（会设置 `isLoading = true`）
- 直接在 `viewModelScope.launch` 中加载数据并更新状态
- 本地数据库操作很快，不需要显示加载指示器

### 3. 修复 PieChart pointerInput key 问题

修改 `PieChart.kt` 的 `PieChartCanvas` 组件：
- 将 `pointerInput(data)` 改为 `pointerInput(data, selectedCategoryId)`
- 确保选中状态改变时重新创建手势检测器，使用最新的 `selectedCategoryId`

### 4. 修复分类列表滚动问题

重构 `StatisticsScreen.kt` 的布局结构：
- 将整个页面从 `Column` 改为 `LazyColumn`
- 使用 `item {}` 包裹单个元素
- 使用 `items(list)` 渲染分类汇总列表
- 确保分类过多时可以正常滚动

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt`
  - 移除 DisposableEffect 和生命周期监听
  - 将 Column 改为 LazyColumn，支持滚动
  - 使用 items() 渲染分类列表

- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModel.kt`
  - 修改 onPeriodSelected() 直接加载数据，不设置 isLoading

- `app/src/main/java/com/example/voicebill/ui/components/PieChart.kt`
  - 修复 pointerInput key，添加 selectedCategoryId

## 验证方法

1. 构建 Debug APK：`gradlew.bat assembleDebug`
2. 安装并打开应用
3. 进入统计页面，点击"今日"、"本周"、"本月"等筛选按钮，观察是否还有闪烁
4. 从统计页面切换到其他页面，再返回统计页面，观察是否还有闪烁
5. 测试饼图点击选中/取消选中是否正常工作
6. 测试分类过多时是否可以正常滚动
