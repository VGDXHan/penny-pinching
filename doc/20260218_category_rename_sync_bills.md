# 分类重命名同步账单

## 问题/需求

分类界面修改分类名称时，需要同步更新该分类下所有账单的 categoryNameSnapshot，保持数据一致性。

## 解决方案

- 在 TransactionDao 中新增 `updateCategoryNameSnapshot` 方法，用于批量更新账单的分类名称快照
- 修改 CategoryRepositoryImpl 的 `updateCategory` 方法，当分类名称变化时自动同步更新账单
- 使用 `database.withTransaction` 包装操作，确保分类更新和账单快照同步的原子性，避免并发问题

## 关键变更

- `app/src/main/java/com/example/voicebill/data/local/dao/TransactionDao.kt` - 新增 updateCategoryNameSnapshot 方法
- `app/src/main/java/com/example/voicebill/data/repository/CategoryRepositoryImpl.kt` - 修改 updateCategory 实现同步更新

## 经验总结

- 审查发现：更新分类和同步快照的操作需要放在事务中，以保证原子性，避免并发更新导致数据不一致
