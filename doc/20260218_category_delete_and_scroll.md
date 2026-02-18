# 分类删除功能优化与统一滚动

## 需求

1. 所有分类（包括默认分类）都支持删除功能
2. 添加"未分类"类别（支出和收入各一个），不可删除
3. 删除分类时，关联的交易记录自动迁移到"未分类"
4. 支出和收入分类作为一个整体进行竖直滑动

## 解决方案

- 新增 `isUncategorized` 字段标识"未分类"类别
- 数据库升级到 version 2，添加迁移逻辑
- 删除分类时使用事务：先迁移交易记录，再软删除分类
- 将两个独立的 `LazyColumn` 合并为一个统一滚动容器
- 语音解析的分类列表改为从数据库动态获取

## 关键变更

- `data/local/entity/CategoryEntity.kt` - 添加 `isUncategorized` 字段
- `domain/model/Category.kt` - 添加 `isUncategorized` 字段
- `data/local/VoiceBillDatabase.kt` - 升级到 version 2，添加 MIGRATION_1_2
- `data/local/dao/CategoryDao.kt` - 新增 `getUncategorizedCategory()` 方法
- `data/local/dao/TransactionDao.kt` - 新增 `migrateCategoryTransactions()` 方法
- `di/DatabaseModule.kt` - 初始化"未分类"分类，注册迁移
- `domain/repository/Repositories.kt` - 新增 `deleteCategoryWithMigration()` 和 `getUncategorizedCategory()` 接口
- `data/repository/CategoryRepositoryImpl.kt` - 实现删除迁移逻辑，使用 `withTransaction` 保证原子性
- `ui/screens/categories/CategoriesScreen.kt` - 统一滚动容器，修改删除按钮逻辑
- `ui/screens/categories/CategoriesViewModel.kt` - 调用 `deleteCategoryWithMigration()`
- `data/repository/BillParserRepositoryImpl.kt` - 动态生成分类列表

## 经验总结

- Room 的 `runInTransaction` 不支持 suspend 函数，需要使用 `withTransaction` 扩展函数
- 数据库迁移时需要先插入新记录再查询其 ID，用于更新关联数据
- Kotlin 字符串中使用中文引号会导致编译错误，需要使用转义的英文引号
