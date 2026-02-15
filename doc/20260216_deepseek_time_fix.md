# 20260216 DeepSeek 时间解析问题修复

## 问题描述

用户反馈：当说"昨天吃烧烤花了53元"时，时间被错误记录为 2024年7月10日0:00。

## 问题原因

DeepSeek API 在解析"昨天"、"上周"等相对时间时，不知道当前时间是多少，导致返回了错误的历史时间戳。代码中只要求 API 将时间转换为毫秒时间戳，但没有提供当前时间作为参考。

## 解决方案

在调用 DeepSeek API 的 prompt 中加入当前时间，让它能根据真实的当前时间计算相对时间。

### 修改文件

`app/src/main/java/com/example/voicebill/data/repository/BillParserRepositoryImpl.kt`

### 修改内容

1. 添加 `LocalDateTime` 导入
2. 在 `parseWithApi` 方法中加入当前时间

```kotlin
val currentTime = LocalDateTime.now(clock)
val prompt = """
    你是一个记账助手，请从用户的记账语句中提取信息。

    重要参考信息：当前时间是 ${currentTime.year}年${currentTime.monthValue}月${currentTime.dayOfMonth}日 ${currentTime.hour}时${currentTime.minute}分

    用户的记账语句：$text
    ...
"""
```

## 构建经验

### Windows 构建命令

正确：
```bash
gradlew.bat assembleDebug
```

错误：
```bash
./gradlew assembleDebug  # Windows 下会报错 "-Xmx64m" ClassNotFoundException
cd /d D:\Cache\daiyu-penny && gradlew.bat assembleDebug  # cd 参数错误
```

### 原因分析

- `./gradlew` 是 Unix 风格的脚本执行方式，Windows 下需要使用 `.bat` 后缀
- Windows 的 `cd /d` 是 CMD 语法，在 bash 环境下不支持

## 后续优化建议

1. 考虑在本地解析成功的情况下，直接使用本地解析结果，避免调用 API（当前逻辑已实现）
2. 可以考虑增加 API 返回时间戳的校验逻辑，确保时间在合理范围内
