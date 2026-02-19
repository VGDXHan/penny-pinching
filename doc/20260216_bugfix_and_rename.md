# 2026-02-16 修改记录

## 1. 修复Bug：记账后统计部分没有数据

### 问题描述
用户在记账页面记账后，切换到统计页面时，统计数据显示为0，没有更新。

### 问题原因
StatisticsScreen 的 ViewModel 只在 `init` 时加载一次统计数据。当用户从记账页面切换到统计页面时，ViewModel 不会重新加载数据。

### 修复方案
1. **StatisticsViewModel.kt** - 添加 `refresh()` 方法用于刷新统计数据

2. **StatisticsScreen.kt** - 添加页面生命周期监听，当页面从后台恢复（ON_RESUME）时自动调用 `refresh()` 刷新数据

### 修改文件
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsViewModel.kt`
- `app/src/main/java/com/example/voicebill/ui/screens/statistics/StatisticsScreen.kt`

---

## 2. 修改应用名称

### 修改内容
将应用名称从"语音记账"改为"penny-pinching"

### 修改文件
- `app/src/main/res/values/strings.xml`

---

## 构建产物
- 生成的APK：`app/build/outputs/apk/debug/app-debug.apk`
