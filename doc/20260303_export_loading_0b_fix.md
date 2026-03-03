# 修复导出一直加载与导出文件 0B

## 问题/需求

设置页执行数据导出时，界面一直显示加载中，导出的 JSON 文件大小为 0B，导出流程未正常结束。

## 解决方案

- 定位导出链路为 `SettingsScreen -> SettingsViewModel -> ExportImportUseCase`。
- 确认根因：`ExportImportUseCase.exportData` 中对 Room `Flow` 使用 `collect`，由于是持续流导致协程不返回，后续写文件逻辑无法执行。
- 将导出数据读取改为一次性快照读取：`getAllCategories().first()`、`getAllTransactions().first()`。
- 保持现有 UI 状态流转逻辑不变，导出方法返回后由 ViewModel 复位 `isExporting`。
- 新增导出单元测试，覆盖长生命周期 Flow、输出流为空、DAO 异常三类场景。

## 关键变更

- `app/src/main/java/com/example/voicebill/domain/usecase/ExportImportUseCase.kt`
  - 导出读取从 `collect` 改为 `first()`，避免导出协程阻塞。
- `app/src/test/java/com/example/voicebill/domain/usecase/ExportImportUseCaseTest.kt`
  - 新增 `ExportImportUseCase` 测试类。
  - 用例 1：长生命周期 Flow 下导出应及时返回且输出非空 JSON。
  - 用例 2：`openOutputStream` 为空时返回错误。
  - 用例 3：DAO 抛异常时返回错误。

## 验证结果

- `./gradlew.bat :app:testDebugUnitTest --tests "*ExportImportUseCaseTest*"` 通过
- `./gradlew.bat :app:testDebugUnitTest --tests "*SettingsViewModelTest*"` 通过
- `./gradlew.bat :app:testDebugUnitTest --tests "*ExportImportUseCaseTest*" --tests "*SettingsViewModelTest*"` 通过

## 经验总结

- 对 Room `Flow` 做一次性数据导出时，应使用 `first()`/`firstOrNull()` 获取快照，避免 `collect` 造成流程不结束。
- 导出类问题应同时验证“返回时机”和“输出内容”，仅验证状态不足以覆盖 0B 文件风险。
