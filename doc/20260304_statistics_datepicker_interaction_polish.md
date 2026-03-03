# 统计页日期选择器交互与样式优化

## 问题/需求

统计页自定义日期范围弹窗存在多处体验问题：

- 日期头文案在窄屏下出现换行、竖排或视觉不对齐，观感较差；
- 顶部周期筛选按钮在小屏机型发生换行；
- 弹窗中的“选择日期/请选择起止日期”文案不够简洁；
- 用户期望“确认”按钮固定在下方居中且始终可见。

## 解决方案

- 顶部周期筛选改为 `LazyRow` 横向布局并居中排列，避免芯片被挤压换行。
- 周期文案缩短为 `日/周/月/年/自定义`，并为文本增加单行约束与省略策略。
- 为 `DateRangePicker` 定制头部文案：
  - 去掉默认标题（不显示“选择日期”）；
  - 头部文案格式统一为短日期区间（如 `2026/3/19 - 2026/3/22`）；
  - 未选择时显示 `开始 - 结束`；
  - 文案保持居中显示。
- 关闭 `DateRangePicker` 的模式切换图标，避免头部出现“视觉假居中”。
- 调整弹窗内部布局：限制日期面板高度并预留底部空间，将“确认”按钮固定在内容区底部居中，始终可见；未选完整区间时按钮置灰。

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt`
  - 周期筛选区域改为 `LazyRow`，并设置居中与单行文本；
  - 新增 `formatDateRangePickerHeadline(...)` 头部文案格式化函数；
  - 自定义 `DateRangePicker` 的 `headline`、隐藏 `title`、关闭 `showModeToggle`；
  - 通过 `heightIn + bottom padding + 底部居中按钮` 重构日期弹窗交互布局。
- `app/src/test/java/com/example/voicebill/ui/screens/statistics/StatisticsScreenDateTextTest.kt`
  - 新增并完善日期头文案格式化单元测试，覆盖空值、单端选择、完整区间与时区转换场景。

## 验证

执行命令：

```bash
./gradlew.bat testDebugUnitTest --tests "com.example.voicebill.ui.screens.statistics.StatisticsScreenDateTextTest" --tests "com.example.voicebill.ui.screens.statistics.StatisticsViewModelTest"
./gradlew.bat assembleDebug
```

结果：均 `BUILD SUCCESSFUL`，并生成最新 debug APK。

## 经验总结

- Material3 `DateRangePicker` 在小屏设备上容易因默认头部与内容高度导致交互元素被挤压，弹窗按钮应优先按“可见性稳定”设计。
- 涉及日期选择类控件时，文案长度与布局约束需要一起设计，单独缩字体通常无法根治换行与错位问题。
