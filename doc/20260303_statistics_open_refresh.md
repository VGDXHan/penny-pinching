# 统计页进入自动刷新修复

## 问题/需求

进入统计页面后，统计数据不会自动刷新，必须手动点击“今日/本周/本月/今年”任一周期按钮才会更新，导致数据时效性和体验不一致。

## 解决方案

- 在统计页组合进入时增加一次页面进入事件，触发 ViewModel 静默刷新。
- 在 `StatisticsViewModel` 中统一加载入口为 `loadStatistics(showLoading: Boolean)`，区分首次加载与静默刷新。
- 首次进入保持加载态；周期切换与页面重进都使用静默刷新，避免闪烁回归。
- 增加并发保护：加载中再次触发进入刷新时直接跳过，避免重复请求。
- 新增 `StatisticsViewModel` 单元测试，覆盖首次加载、进入静默刷新、加载中跳过、周期切换静默刷新。

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt`  
  - 新增 `LaunchedEffect(Unit)`，进入页面调用 `viewModel.onScreenEntered()`。

- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModel.kt`  
  - `init` 改为 `loadStatistics(showLoading = true)`。  
  - `onPeriodSelected()` 改为静默刷新。  
  - 新增 `onScreenEntered()` 页面进入静默刷新方法。  
  - `refresh()` 统一委托到 `onScreenEntered()`。  
  - 新增 `loadStatistics(showLoading: Boolean)` 统一加载逻辑与错误处理策略。

- `app/src/test/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModelTest.kt`  
  - 新增 4 个单元测试用例覆盖关键刷新行为。

## 验证与测试

- `./gradlew.bat app:testDebugUnitTest --tests "*StatisticsViewModelTest"` 通过。
- `./gradlew.bat app:testDebugUnitTest` 全量 JVM 单测通过。

## 经验总结

- 统计页这类“回栈复用页面”场景，不能只依赖 `ViewModel.init`，需要显式处理“页面进入”事件。
- 为避免闪烁，刷新逻辑建议从一开始就区分“首屏加载”和“静默更新”两种路径。