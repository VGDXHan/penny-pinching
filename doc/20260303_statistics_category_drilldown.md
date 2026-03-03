# 统计页分类条目下钻明细

## 问题/需求

统计页面当前只能查看分类汇总（饼图与分类金额），点击分类仅高亮，无法直接查看该分类在当前统计周期内的具体账单条目。
目标是支持在统计页点击分类后下钻到该分类明细，并支持直接编辑/删除条目。

## 解决方案

- 采用页内展开方案：点击分类后，在该分类条目下方展开明细列表。
- 时间口径继承统计周期：明细查询使用统计结果的 `startDate/endDate`，保证口径一致。
- 每个板块（支出/收入）最多展开一个分类；再次点击同分类可收起。
- 复用记录页能力：条目 UI 复用 `TransactionItem`，编辑弹窗复用 `EditTransactionDialog`，编辑/删除逻辑保持一致。
- 数据层新增“按分类+时间区间”查询接口，避免在 UI 层二次过滤。
- 编辑/删除后执行静默刷新统计与明细，避免全屏 loading 闪烁回归。

## 关键变更

- `app/src/main/java/com/example/voicebill/domain/repository/Repositories.kt`
  - `TransactionRepository` 新增 `getTransactionsByCategoryAndDateRange(...)`。
- `app/src/main/java/com/example/voicebill/data/local/dao/TransactionDao.kt`
  - 新增 SQL：`getTransactionsByCategoryAndDateRange(categoryId, startDate, endDate)`。
- `app/src/main/java/com/example/voicebill/data/repository/TransactionRepositoryImpl.kt`
  - 实现新增仓库接口并完成 Entity -> Domain 映射。
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModel.kt`
  - 新增分类明细状态（支出/收入明细、局部 loading）。
  - 新增明细订阅 Job 管理与切换取消逻辑。
  - 新增编辑/删除相关状态与方法，并在操作后静默刷新统计。
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt`
  - 分类项点击后在该项下方渲染 `CategoryTransactionDetailSection`。
  - 明细区域支持加载态/空态/列表态。
  - 接入 `EditTransactionDialog` 与错误弹窗。
- `app/src/test/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModelTest.kt`
  - 新增统计下钻核心单测（展开、收起、周期切换重置、编辑保存、删除刷新）。
- `app/src/test/java/com/example/voicebill/ui/screens/records/RecordsViewModelTest.kt`
  - 更新 `FakeTransactionRepository` 以兼容新增接口。

## 验证与测试

执行命令：

```bash
./gradlew.bat testDebugUnitTest --tests "com.example.voicebill.ui.screens.records.RecordsViewModelTest" --tests "com.example.voicebill.ui.screens.statistics.StatisticsViewModelTest"
```

结果：通过。

期间出现一次 `kspCaches` 损坏导致编译失败，已在项目内清理：

```bash
Remove-Item -Recurse -Force app/build/kspCaches
```

随后重跑测试通过。

## 经验总结

- 统计下钻场景中，明细必须复用统计计算出的时间区间，避免“汇总口径”和“明细口径”不一致。
- 在 Compose 页面中使用局部 loading 替代全屏 loading，可显著降低周期切换/编辑后刷新带来的闪烁感。
- 新增仓库接口后要同步更新测试中的 Fake 实现，避免单测编译层面回归。
