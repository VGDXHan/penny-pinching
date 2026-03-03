# 修复解析页金额输入错位与删除异常

## 问题/需求

- 解析结果区域的金额输入框在编辑时表现异常：字符可能出现在光标前，且在小数点后一位输入时数字可能跳到小数点左侧。
- 用户希望金额输入按正常文本编辑行为工作，并且小数点与 `0` 可以被连续删除。

## 解决方案

- 将解析页金额输入从“数值回填驱动”改为“文本状态驱动”。
- 在 `HomeUiState` 新增 `amountInputText`，专门保存用户编辑中的原始金额文本。
- 金额输入改为调用 `onAmountInputChanged`：
  - 仅允许 `^\d*(\.\d{0,2})?$`（最多 2 位小数）；
  - 合法输入实时同步 `amountInputText` 和 `amount`（分）；
  - 非法输入直接忽略，不重写文本。
- 解析成功后将 `amountCents` 格式化为可编辑文本，写入 `amountInputText`。
- 保存成功后同时清空 `amount` 与 `amountInputText`，避免残留。

## 关键变更

- `app/src/main/java/com/example/voicebill/ui/screens/home/HomeViewModel.kt`
  - 新增状态字段 `amountInputText`
  - 新增金额输入校验与转换函数：`onAmountInputChanged`、`isValidAmountInput`、`parseAmountInputToCents`、`formatCentsToInputText`
  - 解析成功后同步金额文本，保存成功后清空金额文本
- `app/src/main/java/com/example/voicebill/ui/screens/home/HomeScreen.kt`
  - 解析页金额输入框改为绑定 `uiState.amountInputText`
  - `onValueChange` 改为 `viewModel::onAmountInputChanged`
- `app/src/test/java/com/example/voicebill/ui/screens/home/HomeViewModelTest.kt`
  - 新增解析后金额文本同步测试
  - 新增删除小数点与 `0` 的输入行为测试
  - 新增非法输入拦截测试（超 2 位小数、多个小数点）
  - 新增保存后清空解析金额文本测试

## 测试与验证

- 执行并通过：
  - `./gradlew.bat :app:testDebugUnitTest --tests "com.example.voicebill.ui.screens.home.HomeViewModelTest"`

## 经验总结

- 金额输入在 Compose 中应区分“编辑态文本”与“业务数值”，避免每次按键都进行格式化回写导致光标与文本错位。
- 对金额这种强约束输入，采用“轻校验 + 文本保留 + 保存前业务校验”可兼顾可编辑性与数据正确性。
