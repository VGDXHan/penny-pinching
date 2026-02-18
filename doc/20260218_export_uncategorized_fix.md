# 修复导出导入与分类查找 bug

## 问题/需求

合并两个功能分支后，Codex code review 发现两个 bug：
1. 导出/导入数据缺少 `isUncategorized` 字段，导致导入后删除分类失败
2. 解析器返回"未分类"时，收入/支出都有同名分类，导致分类类型匹配错误

## 解决方案

- **P1**: 在 `CategoryExport` 数据类添加 `isUncategorized` 字段，并更新转换函数
- **P2**: 修改 `findCategoryIdByName` 方法，增加 `type` 参数，查找时同时匹配名称和类型

## 关键变更

- `app/src/main/java/com/example/voicebill/data/local/ExportData.kt` - 添加 `isUncategorized` 字段支持导出/导入
- `app/src/main/java/com/example/voicebill/ui/screens/home/HomeViewModel.kt` - 修复分类查找时按类型匹配

## 经验总结

- 导出/导入数据时需考虑所有新增字段，确保 schema 变化能正确迁移
- 当存在同名分类（收入/支出各有一个"未分类"）时，查找逻辑需要额外条件避免歧义
