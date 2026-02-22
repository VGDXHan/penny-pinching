# 迁移手动记账入口并前置校验 API Key

## 问题/需求

- 用户希望“新增条目”入口放在“记账”页，而不是“记录”页。
- 解析失败报错 `Unexpected char ... in Authorization value`，根因是 API Key 含非法字符（如中文）进入了请求头。
- 期望在配置 API Key 时就做校验，避免后续请求阶段才报错。

## 解决方案

- 将手动新增入口（FAB）从 `RecordsScreen` 迁移到 `HomeScreen`。
- 抽取可复用的手动记账弹窗组件，避免 UI 逻辑重复。
- 将手动新增状态与保存逻辑迁移到 `HomeViewModel`，记录页仅保留搜索/编辑/删除职责。
- 新增 `ApiKeyValidator` 统一做 API Key 规范化与合法性校验。
- 在 `SettingsViewModel.saveApiKey()` 保存前校验，不合法直接提示并拒绝保存。
- 在 `BillParserRepositoryImpl` 调用 API 前再次校验历史存量 key，返回业务错误提示，避免底层请求头异常。

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/components/ManualTransactionDialog.kt`  
  抽取手动记账弹窗组件，封装类型/金额/时间/分类/备注输入与保存逻辑。
- `app/src/main/java/com/example/voicebill/ui/screens/home/HomeScreen.kt`  
  新增 FAB，点击打开手动记账弹窗。
- `app/src/main/java/com/example/voicebill/ui/screens/home/HomeViewModel.kt`  
  新增手动创建状态与事件方法（`startCreating`、`saveCreatedTransaction` 等）。
- `app/src/main/java/com/example/voicebill/ui/screens/records/RecordsScreen.kt`  
  移除手动新增 FAB 与创建弹窗入口。
- `app/src/main/java/com/example/voicebill/ui/screens/records/RecordsViewModel.kt`  
  移除手动新增相关状态和方法，聚焦记录管理职责。
- `app/src/main/java/com/example/voicebill/di/ApiKeyValidator.kt`  
  新增 API Key 规范化与合法性校验工具。
- `app/src/main/java/com/example/voicebill/ui/screens/settings/SettingsViewModel.kt`  
  `saveApiKey` 改为前置校验，非法 key 不保存并给出提示。
- `app/src/main/java/com/example/voicebill/di/ApiModule.kt`  
  `SecurePrefs` 改为仅规范化 API Key，不做后置字符清洗。
- `app/src/main/java/com/example/voicebill/data/repository/BillParserRepositoryImpl.kt`  
  调用 DeepSeek 前接入校验，避免 Authorization 非法字符异常。

## 测试与验证

- 新增 `app/src/test/java/com/example/voicebill/di/ApiKeyValidatorTest.kt`，覆盖：
  - 空 key 拒绝
  - 含中文字符拒绝
  - 自动处理 `Bearer ` 前缀
  - 合法 ASCII key 通过
- 新增 `app/src/test/java/com/example/voicebill/ui/screens/settings/SettingsViewModelTest.kt`，覆盖：
  - 非法 key 不保存
  - 合法 key 规范化后保存
- 更新 `app/src/test/java/com/example/voicebill/data/repository/BillParserRepositoryImplTest.kt` 断言与导入，确保现有测试可运行。
- 执行并通过：
  - `./gradlew.bat testDebugUnitTest --tests "com.example.voicebill.di.ApiKeyValidatorTest" --tests "com.example.voicebill.ui.screens.settings.SettingsViewModelTest" --tests "com.example.voicebill.data.repository.BillParserRepositoryImplTest"`

## 经验总结

- API Key 这类敏感配置应在输入/保存阶段做前置校验，防止问题延迟到网络调用阶段暴露。
- 页面职责边界清晰后，测试也应同步按职责重构，避免历史入口迁移后测试语义失真。
