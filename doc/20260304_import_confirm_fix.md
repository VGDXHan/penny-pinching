# 修复导入确认后未执行导入并补充空数据导入测试文件

## 问题/需求

设置页“导入数据”流程存在断链：用户选择 JSON 后会弹出确认框，但点击“确定”没有调用导入逻辑，导致导入无效。  
同时需要一个可直接导入的空 JSON，用于验证“先清空数据，再导回备份数据”的流程。

## 解决方案

- 在设置页增加待导入 `Uri` 状态，文件选择后先缓存 `Uri` 再弹确认框
- 确认导入时调用 `viewModel.importData(uri)`，真正触发导入流程
- 在取消/关闭确认框时清理缓存 `Uri`，避免误用旧引用
- 生成符合当前 schema 的空导入文件 `doc/import_empty.json`
- 重新构建 Debug APK 并验证产物时间
- 执行单元测试任务确认改动未引入回归

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/screens/settings/SettingsScreen.kt` - 修复导入确认流程，新增 `pendingImportUri` 并在确认时执行导入
- `doc/import_empty.json` - 新增空数据导入样例（`categories`、`transactions` 为空数组）

## 验证结果

- `./gradlew.bat testDebugUnitTest` 通过（使用项目内 `GRADLE_USER_HOME`）
- `./gradlew.bat assembleDebug` 通过
- APK 产物：`app/build/outputs/apk/debug/app-debug.apk`（2026-03-04 00:02:55）

## 经验总结

- 涉及 `OpenDocument` 的确认式交互时，必须在弹窗前保存选中的 `Uri`，否则确认动作无法关联到用户刚选的文件。
- 导入覆盖类功能应优先保证“入口链路可达”，再评估是否升级为事务化导入以提升失败场景一致性。
