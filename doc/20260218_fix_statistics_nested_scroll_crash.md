# 统计页点击后闪退（嵌套滚动）修复

## 问题/需求

统计页从底部导航点击进入后仍会闪退，且不重复之前已验证过的占比/进度值排查思路，需要定位新的崩溃原因。

## 解决方案

- 重新检查统计页 UI 结构，确认页面主体已使用 `LazyColumn` 作为外层滚动容器。
- 排查饼图组件后发现 `PieChartLegend` 内部仍使用 `verticalScroll`，形成可滚动容器嵌套。
- 移除图例内部滚动能力，交由外层 `LazyColumn` 统一处理纵向滚动，避免 Compose 的无限高度约束异常。
- 本地执行 Debug 构建验证修复未引入编译问题。

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/components/PieChart.kt` - 删除 `rememberScrollState`/`verticalScroll` 引入与用法，`PieChartLegend` 改为普通 `Column`。

## 经验总结

- Compose 页面中应避免同轴滚动容器嵌套（如 `LazyColumn` 内再放 `verticalScroll`），否则容易在运行时触发约束异常。
- 统计页面这类聚合 UI 组件，应明确“单一滚动责任”，减少后续交互改动引入隐性崩溃风险。
