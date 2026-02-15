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

## 金额存储规范

- 金额以"分"为单位存储 (Long 类型)
- 字段命名: `amountCents: Long`
- UI 展示时除以 100.0，格式化为 "¥128.00"

## DeepSeek API 配置

- API Key 存储在 EncryptedSharedPreferences 中
- API 调用使用 Retrofit，不使用外部 SDK

## 数据导出/导入

- 格式: JSON
- 使用 SAF (Storage Access Framework)
- 导出: `ACTION_CREATE_DOCUMENT`
- 导入: `ACTION_OPEN_DOCUMENT`
- 导入策略: 全量恢复模式（清空本地后导入）
