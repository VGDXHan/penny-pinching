# 统计页崩溃修复

## 问题/需求

点击底部菜单进入“统计”页面时，应用会直接崩溃。  
需要在不改动导航结构的前提下修复崩溃，并保证统计页面对异常历史数据具备安全性。

## 解决方案

- 在统计用例中统一分类占比计算逻辑，只让正金额分类参与占比。
- 使用“正金额分类总和”作为占比分母，避免异常数据导致占比失真。
- 对分类占比统一做安全约束，强制在 `0f..1f` 且为有限值。
- 在统计列表进度条和饼图渲染/点击判定处使用安全占比，避免 Compose 组件因非法进度或角度崩溃。

## 关键变更

- `app/src/main/java/com/example/voicebill/domain/usecase/GetStatisticsUseCase.kt` - 新增 `buildCategorySummaries`，过滤非正金额分类并约束百分比范围。
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt` - `LinearProgressIndicator` 使用安全进度值，避免非法 `progress` 触发崩溃。
- `app/src/main/java/com/example/voicebill/ui/components/PieChart.kt` - 饼图绘制、点击扇区计算、图例百分比显示均使用安全占比。

## 经验总结

- 统计类 UI（进度条、图表）对数值范围敏感，数据层和 UI 层都应做边界保护。
- 对导入历史数据或异常数据场景，优先保证页面“可展示不崩溃”，再逐步收敛业务口径一致性。
