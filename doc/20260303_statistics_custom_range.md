# 统计页自定义时间范围与历史回看

## 问题/需求

统计页面原先仅支持“今日/本周/本月/今年”固定区间，无法查看前几天、前几周、前几个月，也无法手动指定起止日期进行统计。

## 解决方案

- 将统计查询从“仅周期”升级为“查询对象”模式：`StatisticsQuery(period, offset, customRange)`。
- 新增 `CUSTOM` 周期与 `CustomDateRange`，支持手动日期范围统计。
- 抽离 `StatisticsDateRangeResolver` 统一计算 `[start, end)` 时间边界：
  - 日/周/月/年支持按 `offset` 回看历史；
  - 周口径保持周一起始；
  - 自定义范围基于 DateRangePicker 的 UTC 日期毫秒转换为本地日期边界。
- 统计页 UI 增加区间导航卡片：
  - “上一段/下一段”切换历史区间；
  - 到当前区间后“下一段”禁用；
  - 自定义模式下提供“重新选择范围”。
- 新增 `DateRangePicker` 弹窗支持自定义起止日期。
- 保持原有无闪烁策略：筛选切换/翻页时不展示整页 loading。

## 关键变更

- `app/src/main/java/com/example/voicebill/domain/model/Statistics.kt`  
  新增 `StatisticsPeriod.CUSTOM`、`CustomDateRange`、`StatisticsQuery`。
- `app/src/main/java/com/example/voicebill/domain/usecase/StatisticsDateRangeResolver.kt`  
  新增时间范围解析器，封装日/周/月/年偏移与自定义范围换算。
- `app/src/main/java/com/example/voicebill/domain/usecase/GetStatisticsUseCase.kt`  
  入参改为 `StatisticsQuery`，接入统一时间范围解析。
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModel.kt`  
  新增 `periodOffset/customRange/showCustomRangePicker` 状态及相关事件处理。
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt`  
  新增周期导航 UI 与 `DateRangePicker` 弹窗交互。
- `app/src/test/java/com/example/voicebill/domain/usecase/GetStatisticsUseCaseTest.kt`  
  新增统计用例层测试，覆盖偏移与自定义区间边界。
- `app/src/test/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModelTest.kt`  
  新增 ViewModel 状态流与查询参数测试。

## 验证

执行命令：

```bash
./gradlew.bat testDebugUnitTest --tests "com.example.voicebill.domain.usecase.GetStatisticsUseCaseTest" --tests "com.example.voicebill.ui.screens.statistics.StatisticsViewModelTest"
```

结果：`BUILD SUCCESSFUL`。

## 经验总结

- 时间区间计算建议集中封装，避免 UI/ViewModel/UseCase 多处重复口径。
- 自定义日期范围要明确语义为“起止日均包含”，查询层统一转为 `[start, nextDayOfEnd)` 更稳妥。
- 统计页涉及图表与滚动容器，新增交互时要持续遵循“单一滚动责任”以防回归崩溃。
