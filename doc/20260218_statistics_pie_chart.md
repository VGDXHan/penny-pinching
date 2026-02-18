# 统计界面饼状图功能

## 需求

在统计界面添加按时间段（今日/本周/本月/今年）的分类饼状图可视化，支持点击高亮交互。

## 解决方案

- 使用 Jetpack Compose Canvas 自定义绘制饼图，无需第三方依赖
- 饼图/图例/列表项三向联动高亮
- 选中时使用白色描边效果（避免偏移导致点击检测坐标系不一致）
- 最后一个扇区扩展到 360° 处理浮点误差

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/theme/Color.kt` - 添加 PieChartColors（12 种颜色）
- `app/src/main/java/com/example/voicebill/ui/components/PieChart.kt` - 新建饼图组件（PieChartCard、PieChartCanvas、PieChartLegend）
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModel.kt` - 添加 selectedExpenseCategoryId/selectedIncomeCategoryId 状态管理
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt` - 集成饼图，CategorySummaryItem 支持点击和高亮

## 经验总结

- Canvas 绘制饼图时，选中效果使用描边而非偏移，可避免点击检测坐标系不一致问题
- 浮点数累加可能导致最后一个扇区角度不足 360°，需要特殊处理
- IntSize 没有 minDimension 属性，需要用 minOf(width, height) 替代
