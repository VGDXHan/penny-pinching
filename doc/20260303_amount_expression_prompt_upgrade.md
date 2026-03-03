# DeepSeek 金额表达式解析与提示词增强

## 问题/需求

语音记账在同一句包含多个金额时会误判总额，例如“今天买了一杯小米粥，36块，还有一份12块的炒面”原实现只取首个金额，导致结果为 36 元而非 48 元。  
同时，模型在“元/分”换算场景中偶发输出单位错误，需要通过提示词与程序双重约束提升稳定性。

## 解决方案

- 升级 DeepSeek `system prompt`，新增 `amountExpression` 字段，并明确金额规则：
  - 多金额默认相加；
  - 折扣转乘法；
  - 返现/退款等转减法；
  - `amountCents = round(expression * 100)`；
  - 保留并强化原有时间解析规则。
- 在仓库层引入 `ApiParseResult`，将 `BillInfo` 与 `amountExpression` 一并承载，避免丢失表达式信息。
- 新增金额表达式计算器 `AmountExpressionEvaluator`，支持 `+ - * / ()`、空白符、负号、除零保护与四舍五入转分。
- 合并策略调整为：`amountExpression计算值 > 本地解析金额 > API amountCents`，表达式非法时自动回退本地金额。

## 关键变更

- `app/src/main/java/com/example/voicebill/data/repository/BillParserRepositoryImpl.kt`
  - 更新 `buildSystemPrompt`，加入金额表达式与换算硬规则；
  - `parseWithApi/parseApiResponse/extractJsonFromResponse` 改为返回 `ApiParseResult`；
  - `mergeLocalAndApiResult` 接入表达式优先合并逻辑。
- `app/src/main/java/com/example/voicebill/data/repository/AmountExpressionEvaluator.kt`
  - 新增四则运算表达式求值器并输出分单位金额。
- `app/src/test/java/com/example/voicebill/data/repository/BillParserRepositoryImplTest.kt`
  - 适配 `amountExpression` 字段；
  - 新增“表达式优先”和“表达式非法回退本地”测试。
- `app/src/test/java/com/example/voicebill/data/repository/AmountExpressionEvaluatorTest.kt`
  - 新增混合运算、括号、非法表达式测试。

## 验证结果

执行命令：

```bash
./gradlew.bat :app:testDebugUnitTest --tests "com.example.voicebill.data.repository.AmountExpressionEvaluatorTest" --tests "com.example.voicebill.data.repository.BillParserRepositoryImplTest"
```

结果：通过。

## 经验总结

- 仅依赖提示词很难完全规避金额单位与算术误差，关键金额字段应在程序端做确定性计算。
- “模型抽取表达式 + 程序计算结果”的分工更适合金额场景，可同时兼顾灵活理解与稳定准确。
