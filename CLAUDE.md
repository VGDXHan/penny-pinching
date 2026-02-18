# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个 Android 语音记账应用 (penny-pinching)，通过自然语言（语音/文本）快速记账，自动提取金额、分类、时间等信息，支持分类管理和消费统计，数据本地存储并支持导出/导入。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **架构**: MVVM + Clean Architecture
- **数据库**: Room
- **API**: DeepSeek (通过 Retrofit 调用)
- **DI**: Hilt
- **异步**: Kotlin Coroutines + Flow

## 常用命令

```bash
# 构建 Debug APK (Windows)
gradlew.bat assembleDebug

# 构建 Release APK (Windows)
gradlew.bat assembleRelease

# 构建 Debug APK (Linux/Mac)
./gradlew assembleDebug

# 运行测试
./gradlew test

# 清理构建
./gradlew clean
```

## 项目架构

```
app/src/main/java/com/example/voicebill/
├── di/                    # Hilt 依赖注入模块
│   ├── AppModule.kt
│   ├── ApiModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── data/                  # Data Layer
│   ├── local/             # Room 数据库
│   │   ├── entity/        # 数据库实体
│   │   ├── dao/           # DAO 接口
│   │   └── VoiceBillDatabase.kt
│   ├── remote/            # DeepSeek API 调用
│   │   ├── DeepSeekApi.kt
│   │   └── DeepSeekModels.kt
│   └── repository/        # Repository 实现
├── domain/                # Domain Layer
│   ├── model/             # 领域模型
│   │   ├── Transaction.kt
│   │   ├── Category.kt
│   │   ├── TransactionType.kt
│   │   ├── BillInfo.kt
│   │   └── Statistics.kt
│   ├── repository/        # Repository 接口定义
│   └── usecase/           # 业务用例
└── ui/                    # UI Layer
    ├── theme/             # Compose 主题
    ├── navigation/         # 导航
    ├── components/         # 通用组件
    └── screens/           # 页面
        ├── home/          # 首页（记账）
        ├── categories/    # 分类管理
        ├── records/       # 记录列表
        ├── statistics/    # 统计
        └── settings/      # 设置
```

# 开发历程记录

> 文档索引，需要详细信息时读取对应文档

- **2026-02-15** [init_plan.md](doc/20260215_init_plan.md) - 项目架构设计、技术栈选型、模块划分、数据模型、实施步骤
- **2026-02-16** [bugfix_and_rename.md](doc/20260216_bugfix_and_rename.md) - 修复统计页面不刷新、应用重命名为 penny-pinching
- **2026-02-16** [deepseek_time_fix.md](doc/20260216_deepseek_time_fix.md) - 修复 DeepSeek 相对时间解析错误、Windows 构建命令经验
- **2026-02-18** [datetime_picker.md](doc/20260218_datetime_picker.md) - 添加日期时间选择功能、修复 API Key 换行符问题、修复保存按钮被遮挡、显示解析时间功能
- **2026-02-18** [category_delete_and_scroll.md](doc/20260218_category_delete_and_scroll.md) - 分类删除功能优化、添加"未分类"类别、统一滚动容器、语音解析动态分类
- **2026-02-18** [statistics_pie_chart.md](doc/20260218_statistics_pie_chart.md) - 统计界面添加分类饼状图可视化，支持点击高亮交互
- **2026-02-18** [export_uncategorized_fix.md](doc/20260218_export_uncategorized_fix.md) - 修复导出导入缺少 isUncategorized 字段、修复分类查找时收入/支出"未分类"混淆问题
- **2026-02-18** [category_rename_sync_bills.md](doc/20260218_category_rename_sync_bills.md) - 分类重命名时同步更新账单名称快照

