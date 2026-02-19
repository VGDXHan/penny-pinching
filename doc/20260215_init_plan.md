# 安卓自然语言记账系统 - 实现计划

## 1. 项目概述

开发一个安卓本地应用，通过自然语言（语音/文本）快速记账，自动提取金额、分类、时间等信息，支持分类管理和消费统计，数据仅本地存储并支持导出/导入。

## 2. 技术栈

| 组件 | 技术方案 |
|------|----------|
| 开发语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| 数据库 | Room |
| API 调用 | OpenAI SDK (DeepSeek兼容) |
| DI | Hilt |
| 异步 | Kotlin Coroutines + Flow |
| 导出格式 | JSON |

## 3. 核心功能模块

### 3.1 自然语言记账
- **输入方式**: 文本输入（利用系统输入法的语音输入按钮）
- **信息提取**: 调用 DeepSeek API 解析
  - 金额（支持中文数字转换："一百五" → 150，单位：分）
  - 分类（餐饮/交通/购物/工资/红包/其他）
  - 时间（支持"今天"、"昨天"、"上周三"等自然语言）
  - 备注（可选）
- **时间解析规则**:
  - 以用户**本地时区**解析
  - "上周"以**周一**为一周起点
  - 引入 `Clock` 注入，单测可用固定"当前时间"
- **JSON 解析策略**:
  - 宽松容错：忽略未知字段、允许缺字段为 null
  - 重试约束：失败后用更强约束提示（如：只输出 JSON、不要解释文本）
- **示例**:
  - "今天中午吃火锅花了128元" → 金额:12800分, 分类:餐饮, 时间:今天, 备注:火锅
  - "给车加油400" → 金额:40000分, 分类:交通

### 3.6 DeepSeek API Key 管理
- **存储**: EncryptedSharedPreferences（加密存储）
- **安全策略**: 不使用明文存储
- **离线兜底**: 网络不可用时仍能手动记账

### 3.2 分类管理
- **默认分类**: 餐饮、交通、购物、工资、红包、其他
- **功能**:
  - 查看所有分类
  - 添加自定义分类
  - 编辑分类名称
  - 删除分类（软删除，保留历史记录）
  - 分类图标/颜色自定义

### 3.3 记账记录管理
- **功能**:
  - 列表展示所有记账记录
  - 按日期/分类筛选
  - 编辑记录
  - 删除记录
  - 搜索功能

### 3.4 统计功能
- **按日/周/月/年统计**
- **图表展示**: 饼图（分类占比）、折线图（趋势）
- **分类汇总**: 每个分类的总支出/收入

### 3.5 数据导出/导入
- **导出**: 使用 SAF (Storage Access Framework)
  - 使用 `ACTION_CREATE_DOCUMENT` 让用户选择保存位置和文件名
  - 写入 `content://` URI
- **导入**: 使用 SAF
  - 使用 `ACTION_OPEN_DOCUMENT` 选择 JSON 文件
  - 使用 `takePersistableUriPermission` 保留访问权限
  - **导入前弹确认对话框**：会覆盖本机数据
  - 使用 Room 事务批量写入，失败回滚
- **JSON 格式**:
```json
{
  "schemaVersion": 1,
  "exportedAt": 1699999999999,
  "appVersion": "1.0.0",
  "categories": [...],
  "transactions": [...]
}
```
- **导入策略**: 全量恢复模式（清空本地后导入）
- **ID 处理**: 原样使用导出文件的 `id`，Room DAO 允许插入指定 id
- **测试**: 覆盖 JSON 结构 + 导入事务逻辑（mock stream），SAF 文件选择需 UI 手动验证

## 4. 数据模型

```
Transaction (记账记录)
├── id: Long (主键)
├── amountCents: Long (金额，单位：分)
├── categoryId: Long (分类ID)
├── categoryNameSnapshot: String (分类名称快照，软删除后仍可读)
├── type: Enum (收入/支出)
├── date: Long (交易发生时间，用户说"昨天/上周三"解析后的时间)
├── note: String? (备注)
├── rawText: String (原始输入)
└── createdAt: Long (录入时间)

Category (分类)
├── id: Long (主键)
├── name: String (名称)
├── icon: String (图标名)
├── color: String (颜色)
├── isIncome: Boolean (是否收入分类)
├── isDefault: Boolean (是否默认分类)
└── isDeleted: Boolean (软删除标记)
```

### 4.1 金额存储规范
- **DB 存储**: Long (最小货币单位/分)，如 128 元存为 12800
- **字段命名**: `amountCents: Long`
- **JSON 导出**: amountCents (分)
- **UI 展示**: amountCents / 100.0，格式化为 "¥128.00"
- **LLM 解析**: 输出整数分或"元+小数"，本地统一转为分

### 4.2 统计口径规范
- **金额**: 始终为正数
- **收入/支出**: 按 `type` 字段分流统计
- **SQL 聚合**: WHERE type = 'INCOME' / WHERE type = 'EXPENSE'

## 5. 架构设计

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  (Compose Screens + ViewModels)     │
├─────────────────────────────────────┤
│         Domain Layer                │
│  (Use Cases + Repository Interfaces)│
├─────────────────────────────────────┤
│          Data Layer                 │
│  (Room DB + API Service + Repos)    │
└─────────────────────────────────────┘
```

## 6. 模块划分

项目拆分为 4 个独立模块，每个模块独立开发、测试：

### 6.1 LLMBillParser
- **职责**: DeepSeek API 接入，自然语言解析
- **所属层**: Data Layer (Remote DataSource)
- **输出**: 金额、分类、时间、备注
- **接口**: `BillParseRepository.parseBillText(text: String): BillInfo`
- **解析兜底策略**:
  - 要求 LLM 输出固定 JSON 格式
  - 本地 JSON 解析校验；失败则重试一次
  - 解析失败：保留 rawText，回到手动填写
  - 分类不确定：默认为"其他"
  - 金额缺失：不自动入库，提示用户补全
- **分类映射策略**:
  - 标准分类词表 + 同义词表（餐饮/吃饭/午餐/火锅 → 餐饮）
  - 找不到匹配：落到"其他"
  - 入库时写入 `categoryId` + `categoryNameSnapshot`
- **测试**: Mock API 响应，验证解析结果

### 6.2 BillRecorder
- **职责**: 记账记录 CRUD，分类管理
- **接口**: `TransactionRepository`, `CategoryRepository`
- **测试**: Room 数据库单元测试

### 6.3 BillAnalyser
- **职责**: 按日期/分类统计，图表数据生成
- **接口**: `AnalysisRepository` (查询统计结果)
- **测试**: 验证统计计算逻辑

### 6.4 DataManager
- **职责**: 数据导出/导入（JSON 格式）
- **接口**:
  - `exportData(targetUri: Uri): ExportResult`
  - `importData(sourceUri: Uri): ImportResult`
- **SAF 实现**: 使用 content:// Uri，不是真实 File 路径
- **测试**: 验证 JSON 序列化/反序列化

### 6.5 模块依赖关系

```
┌─────────────────────────────────────┐
│              UI Layer               │
│   (Compose Screens + ViewModels)    │
└──────────────┬──────────────────────┘
               │
    ┌──────────┴──────────────────────┐
    │      Domain Layer               │
    │  (Use Cases)                    │
    │  - ParseBillTextUseCase         │
    │  - BillRecorderUseCase          │
    │  - BillAnalyserUseCase         │
    │  - DataManagerUseCase          │
    └──────────────┬──────────────────────┘
               │
    ┌──────────┴──────────┐
    │     Data Layer      │
    │  - Repository Impl  │
    │  - Room DB           │
    │  - LLM Remote API    │
    └─────────────────────┘
```

**依赖方向**:
- Domain → Data (Repository 接口)
- Data Layer: LLM API / Room DB / Repository 实现
- **LLMBillParser 属于 Data Layer (Remote DataSource)**

## 8. 关键文件结构

```
app/src/main/java/com/example/voicebook/
├── di/                    # Hilt 模块
├── data/
│   ├── local/            # Room 数据库
│   ├── remote/           # API 服务
│   └── repository/       # Repository 实现
├── domain/
│   ├── model/            # 数据模型
│   ├── repository/       # Repository 接口
│   └── usecase/          # 业务用例
└── ui/
    ├── theme/            # Compose 主题
    ├── screens/          # 页面
    │   ├── home/         # 首页（记账）
    │   ├── categories/  # 分类管理
    │   ├── records/     # 记录列表
    │   └── statistics/  # 统计
    └── components/      # 通用组件
```

## 7. 实施步骤

**每个模块独立完成，单元测试通过后再开发下一个模块，最后集成**

---

### Phase 0: 环境搭建（首次）✓ 完成

**环境状态**:
- JDK: ✓ JDK 21
- Android Studio: ✓ 已安装
- Android SDK: ✓ API 35 (Android 15) @ D:\Soft\Android_Studio_SDK

---

### Phase 1: 基础架构

1. 创建项目骨架，配置 Gradle
2. 配置 Android SDK 路径 (local.properties)
3. 设置 Hilt、Room、Compose 依赖
4. 创建数据模型和 Room 数据库

- **测试**: [自动] 运行 `assembleDebug` 编译验证

---

### Phase 2: 模块 1 - LLMBillParser

**所属层**: Data Layer (Remote DataSource)

1. 接入 OpenAI SDK
2. 设计 Prompt（要求输出固定 JSON 格式）
3. 实现解析 + 兜底逻辑
   - JSON 解析校验；失败重试一次
   - 解析失败：保留 rawText，回到手动填写

- **测试**: [自动] Mock API 响应，验证解析结果正确

---

### Phase 3: 模块 2 - BillRecorder

**所属层**: Domain Layer + Data Layer (Room)

1. Room 数据库设计
2. Repository 接口定义 + 实现
3. 分类和记账记录 CRUD
4. 分类软删除 + 快照存储

- **测试**: [自动] Room CRUD 单元测试

---

### Phase 4: 模块 3 - BillAnalyser

**所属层**: Domain Layer (Use Case)

1. 使用 Room SQL 聚合（GROUP BY, SUM）做主统计
2. Domain 层做轻量二次处理（生成图表点）
3. 饼图/折线图数据生成

- **测试**: [自动] 统计计算逻辑单元测试

---

### Phase 5: 模块 4 - DataManager

**所属层**: Domain Layer (Use Case)

1. JSON 序列化（含 schemaVersion + exportedAt）
2. SAF 导出（ACTION_CREATE_DOCUMENT）
3. SAF 导入（ACTION_OPEN_DOCUMENT + 全量恢复模式）

- **测试**: [自动] JSON 序列化一致性测试

---

### Phase 6: 集成与 UI

1. 组合各模块（依赖注入）
2. Compose UI 实现
3. DeepSeek API Key 配置界面

- **测试**: [手动] 端到端测试（你点击运行 APK 验证）

## 8. 验证方式

**在电脑上完成测试，确认无误后再打包 APK**

| 测试类型 | 执行方式 | 说明 |
|----------|----------|------|
| **单元测试** | [自动] Android Studio 自动运行 | 我写代码，你点击运行按钮 |
| **编译验证** | [自动] `assembleDebug` | 确保代码能编译通过 |
| **端到端** | [手动] 你点击运行 APK | 最终验证所有功能 |

### 测试流程
1. 每个模块完成后，我编写单元测试
2. 你在 Android Studio 点击运行
3. 绿色勾 = 通过，继续下一个模块
4. 所有模块通过后，打包 APK 你安装验证
