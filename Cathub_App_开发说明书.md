# 安卓猫咪识别与信息管理 App ——开发说明书（Markdown）
> 版本：v2025-11-06（面向 Android 手機）  
> 项目目标：支持录入猫咪档案、端侧识别与信息回显、状态上报与组织协作，前端风格为**平面化极简（纯白基调、线框 UI）**。

---

## 1. 总览（TL;DR）
- **端侧识别路线**：CameraX 取流 → 目标检测（TFLite EfficientDet-Lite0/Task Library `ObjectDetector`）→ 裁剪猫脸/身体 → 图像嵌入（Task Library `ImageEmbedder`）→ 向量检索（HNSW/FAISS/小规模暴力检索）→ 阈值/多样本融合 → 命中猫咪档案并展示。citeturn9search0turn2search3turn2search4  
- **同步与协作**：资料与状态事件存于 **Firebase Firestore**（默认离线缓存 + 实时订阅），照片存 **Firebase Storage**，通知走 **FCM**。citeturn2search5turn2search6turn2search7  
- **UI 风格**：纯白背景、单像素线框、几何留白，大字号标题、等距网格，Material3 组件定制为“线框风”。
- **技术栈**：Kotlin 2.2.21、AGP 8.13、Gradle 8.13、Jetpack Compose（BOM 管理）、Navigation 2.9.5、CameraX 1.5.1、Coil 3。citeturn5search1turn1search6turn1search3turn6search0turn8search4turn7view0turn1search5  
- **发布要求**：自 **2025-08-31** 起，新上架/更新必须 **targetSdk = 35 (Android 15)**；存量应用需至少 target 34 才能对高版本设备可见（可申请延期至 2025-11-01）。citeturn12search0

---

## 2. 目标与范围
### 2.1 用户故事（简）
- 录入者：为每只猫添加**照片、名字、性别、年龄、花色、活动区域、性格、食物喜好与投喂建议**；可后续补充多角度照片作为识别样本。
- 巡护者：打开“识别”→ 对准猫 → App 显示匹配档案与**投喂建议**；若无匹配，则**引导创建新档案**或追加样本。
- 管理组织：实时接收**受伤/生病/绝育等状态**上报；可查看**时间线**与**地理热区**（基于活动区域或上报点）。

### 2.2 非目标
- 不做复杂行为分析（如自动姿态、活动量）；不做跨端后台控制台（可后续扩展 Web）。

---

## 3. 架构与模块化
### 3.1 分层
- **UI（:app-ui）**：Compose/Material3，MVVM（ViewModel + State/Effect）。
- **Domain（:core-domain）**：用例（UseCase）、实体（Cat、Sighting、HealthReport…）。
- **Data（:core-data）**：仓库（Repo），Firestore/Storage/FCM 封装，Room（可选本地缓存）。
- **ML（:feature-ml）**：检测与嵌入推理封装，内置阈值与多样本合并策略。
- **Features**：`:feature-cat-profile`（档案）、`:feature-detection`（识别）、`:feature-reporting`（上报）、`:feature-feeding`（投喂）。

### 3.2 依赖与事件流
- UI → UseCase → Repository →（Firestore/Storage/ML）  
- 上报通过 `SightingRepository.report(...)` / `HealthRepository.report(...)`，触发 Firestore 写入与可选 FCM Topic 推送（如 `cats/{catId}` 订阅）。citeturn2search7

---

## 4. 端侧识别方案（关键）
### 4.1 视觉流水线
1) **CameraX 预览 + 分析**（`ImageAnalysis`，30 FPS 限流至 ~15 FPS）。CameraX 1.5.1 是当前稳定分支。citeturn7view0  
2) **目标检测**：TFLite Task Library `ObjectDetector`（移动端推荐做法），模型用 **EfficientDet-Lite0**（精度/速度平衡）。citeturn9search0turn2search9  
3) **裁剪**：对检测框扩边并等比缩放。
4) **图像嵌入**：Task Library `ImageEmbedder` 生成 128/256/512 维向量，用余弦相似度。citeturn2search3  
5) **向量检索**：
   - 小规模（≤3k 向量）：内存暴力检索（批量 `dot`/`cos`）。
   - 中规模（3k–50k）：**HNSW**（ObjectBox Vector Search Android 可用）。citeturn10search0
   - 进阶：NDK 集成 **FAISS**（arm64），但需自行编译与体积评估。citeturn10search2turn10search5
6) **决策**：Top‑1 相似度 ≥ 设定阈值即命中；若 Top‑2 接近，要求人工确认。
7) **多样本融合**：每只猫存**多向量中心**（聚类质心或指数滑动平均），提升鲁棒性。

### 4.2 模型与数据
- 检测：`efficientdet_lite0.tflite`（COCO 预训练含 *cat* 类），如现场误检多，可自采样微调。citeturn2search4  
- 嵌入：选标准通用图像嵌入（TF Hub 兼容），或自训“猫脸/花色”子域模型提高区分度。citeturn2search3
- 标注建议：每只猫 **≥10 张**不同角度/光照/距离；定期淘汰过时样本。

### 4.3 性能预算（中端机）
- 目标检测 **≤12ms**/帧（CPU/GPU 二选一），嵌入 **≤8ms**/裁剪图。整体推理 **≤25ms**。  
- 首帧启动：用 **Baseline Profiles** + 冷启动优化（目标 **TTFD ≤1200ms**）。citeturn4search1turn4search2turn4search3

---

## 5. 数据模型（Firestore 建议）
> 集合/文档命名均小写复数，字段使用 `snake_case`。Firestore 默认**离线持久化开启**，可离线读写、上线自动同步。citeturn2search5

- `cats/{cat_id}`  
  ```json
  {
    "name": "雪球",
    "sex": "female|male|unknown",
    "age_months": 18,
    "pattern": "三花/狸花/黑白…",
    "activity_areas": ["小区东门", "停车场"],
    "personality": ["温顺","胆小"],
    "food_preferences": ["鸡胸肉", "幼猫粮"],
    "feeding_tips": "避免乳制品；少量多餐",
    "photos": [ { "gcs_path": "...", "uploaded_by": "uid", "ts": 1730840000 } ],
    "embeddings": [ { "vec": [0.1, ...], "source_photo": "gcs_path", "ts": 1730840000 } ],
    "created_by": "uid",
    "created_at": 1730830000,
    "updated_at": 1730840000
  }
  ```
- `sightings/{sighting_id}`（识别或人工标注一次“遇见”）  
  `cat_id | photo | location (optional) | similarity | device | reporter | ts`
- `health_reports/{report_id}`（受伤/生病/绝育等）  
  `cat_id | type | severity | note | photos[] | reporter | ts | status`
- `feed_logs/{log_id}`（投喂记录）  
  `cat_id | food | qty | note | reporter | ts`
- `users/{uid}`：权限、加入组织信息。

**存储**：原图与裁剪图放 **Firebase Storage**；上传后将 `gcs_path` 回写 Firestore。citeturn2search6

**通知**：组织管理员订阅 `topic:org_{orgId}` 或 `topic:cat_{catId}`，收取上报/识别事件。citeturn2search2

---

## 6. 权限与隐私
- **相机**：`android.permission.CAMERA`。
- **图库（Android 13+）**：读取图片需 `READ_MEDIA_IMAGES`；Android 14 支持“选择性照片访问”，追加 `READ_MEDIA_VISUAL_USER_SELECTED` 处理**重新选择**。citeturn3search2turn11search4
- **通知（Android 13+）**：`POST_NOTIFICATIONS` 运行时权限。citeturn3search1
- **照片选择器**：优先使用系统 Photo Picker（含向后移植），减少权限请求与合规风险。citeturn11search5

**隐私提示**：推送使用的“Push Token/元数据”可能受平台合规监管而被调取，应避免与可识别个人信息直接绑定并最小化日志。citeturn2news61

---

## 7. UI / 交互规范（极简线框风）
- **色彩**：纯白 `#FFFFFF` 为背景；文本仅两层（主文本 87% 黑、辅助 60% 黑）；线框 `#E0E0E0`。
- **网格**：8dp 基线网格，卡片圆角 16–20dp，单像素描边。
- **组件**：Material3 组件全部采用“**outlined/tonal**”变体；按钮和输入框保留轮廓线。
- **关键页面**：
  - 主页（三分栏卡片）：**识别** / **上报** / **档案**。
  - 识别页：顶部取景器（1:1 或 4:3），底部“命中卡片”抽屉（照片、名字、花色、活动区域、性格、食物喜好与投喂建议）。
  - 档案详情：信息分组；**“添加样本”**悬浮按钮；时间线（上报 / 投喂 / 识别记录）。
  - 上报向导：受伤 / 生病 / 其他 → 文字 + 照片 → 提交。

---

## 8. 关键用例流程
### 8.1 录入猫咪档案
1) Photo Picker 选择图片（或相机拍摄）。  
2) 表单校验 → 上传 Storage → 回写 `photos[]`。  
3) 生成嵌入向量并存入 `embeddings[]`。

### 8.2 端侧识别
1) CameraX 预览 → 每 N 帧执行检测；  
2) 对每个框裁剪 → 计算嵌入 → 向量检索；  
3) 若命中：拉取 `cats/{id}` 并显示，附**投喂建议**；未命中：提示“创建新猫”或“将样本追加至指定猫”。

### 8.3 状态上报 / 通知
- 上报写入 `health_reports` / `sightings`；Cloud Functions（可选）转发 FCM 到组织 Topic。citeturn2search2

---

## 9. 构建与依赖（建议模板）
> 尽量使用 **BOM** 管理版本，减少不兼容风险（Compose BOM 示例版本见官方文档）。citeturn6search0

**Gradle / Kotlin / AGP**
```kts
// gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-all.zip
```
```kts
// settings.gradle.kts
pluginManagement { repositories { gradlePluginPortal(); google(); mavenCentral() } }
```
```kts
// build.gradle.kts (project)
plugins { id("com.android.application") version "8.13.0" apply false } // AGP
```
```kts
// app/build.gradle.kts
plugins {
  id("com.android.application")
  kotlin("android") version "2.2.21"
  id("com.google.gms.google-services")
}
android {
  namespace = "com.example.cats"
  compileSdk = 35
  defaultConfig {
    applicationId = "com.example.cats"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"
  }
}
dependencies {
  // Compose with BOM
  val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
  implementation(composeBom); androidTestImplementation(composeBom)
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.activity:activity-compose:1.9.3")

  // Navigation
  implementation("androidx.navigation:navigation-compose:2.9.5")

  // CameraX (stable line)
  implementation("androidx.camera:camera-camera2:1.5.1")
  implementation("androidx.camera:camera-lifecycle:1.5.1")
  implementation("androidx.camera:camera-view:1.5.1")

  // Image loading (Coil 3)
  implementation("io.coil-kt:coil3:3.3.0")
  implementation("io.coil-kt:coil3-compose:3.3.0")

  // Firebase (BoM)
  implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
  implementation("com.google.firebase:firebase-firestore")
  implementation("com.google.firebase:firebase-storage")
  implementation("com.google.firebase:firebase-messaging")
  implementation("com.google.firebase:firebase-crashlytics")
  implementation("com.google.firebase:firebase-analytics")

  // TFLite Task Library (ObjectDetector / ImageEmbedder)
  implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
  implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0") // 可选

  // HNSW 向量检索（可选，或自研）
  // implementation("io.objectbox:objectbox-android:3.8.0")

  // 性能
  implementation("androidx.profileinstaller:profileinstaller:1.4.1")
  androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.3.4")
}
```

**版本来源与说明**：Kotlin 2.2.21（GitHub 发布）；AGP 8.13（官方表）；Gradle 8.13（官方发行说明）；Compose BOM 使用法（官方）；Navigation 2.9.5（官方版本页）；CameraX 稳定 1.5.1（官方版本页）；Coil 3（官方迁移/发布）；Firebase BoM 34.5.0（官方映射）。citeturn5search1turn1search6turn1search3turn6search0turn8search4turn7view0turn1search5turn2search6

---

## 10. 安全 & 规则
**Storage 规则（示例，需按组织/用户鉴权细化）**：
```
// storage.rules
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /cats/{catId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.resource.size < 8 * 1024 * 1024;
    }
  }
}
```
**Firestore 规则（简化示例）**：
```
// firestore.rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function authed() { return request.auth != null }
    match /cats/{catId} {
      allow read: if authed();
      allow create, update: if authed();
    }
    match /health_reports/{id} {
      allow read, create: if authed();
      allow update: if authed();
    }
    match /sightings/{id} {
      allow read, create: if authed();
    }
  }
}
```

---

## 11. 性能与质量保障
- **Baseline Profiles**：集成 `profileinstaller`，用 Macrobenchmark 生成/验证，显著缩短冷启动与首帧时间。citeturn4search1turn4search2turn4search3
- **图像管线**：上传前压缩到长边 ≤ 1280px；Coil 3 配置内存缓存与缩略图占位。citeturn1search5
- **离线体验**：Firestore 默认离线缓存；上报在无网时进入**本地队列**，联网自动回放。citeturn2search5

---

## 12. 合规与上架
- **targetSdk**：须满足 2025 年政策（target 35）；若需过渡可申请延至 2025‑11‑01。citeturn12search0
- **运行时权限**：Android 13+ 通知 `POST_NOTIFICATIONS`；媒体读取权限细分；Android 14 “选择性照片访问”。citeturn3search1turn3search2turn11search4

---

## 13. 测试计划（关键用例）
- **识别准确性**：测试集按猫划分 70/30；报告 Top‑1 命中率、Top‑1–Top‑2 间隔分布；阈值自动化回归。
- **弱光/遮挡**：室外夜间、逆光、雨天样本；检测/嵌入耗时统计。
- **离线/弱网**：飞行模式下录入/上报/识别 → 联网后正确回放与去重。
- **权限回归**：首次启动、拒绝后引导、Android 14 重新选择照片流程。

---

## 14. 里程碑（建议）
1) 原型（2 周）：完成 UI 骨架、数据模型、手工录入与列表；  
2) 识别（3 周）：接入检测 + 嵌入 + 检索、命中展示；  
3) 上报/通知（2 周）：健康/目击上报、Topic 推送；  
4) 性能/发布（2 周）：Baseline Profiles、冷启动/帧率优化、Play 合规。

---

## 15. 参考与链接
- CameraX 稳定版/版本页（1.5.1 稳定行）。citeturn7view0  
- Compose BOM 与版本映射。citeturn6search0turn6search1  
- Navigation 2.9.5 发布页。citeturn8search4  
- Firestore 离线默认开启（Android/iOS）。citeturn2search5  
- Firebase Android BoM 与映射。citeturn2search6  
- FCM 主题消息（Topic）。citeturn2search2  
- TFLite Task Library：ObjectDetector / ImageEmbedder。citeturn9search0turn2search3  
- EfficientDet-Lite0（TF Hub/TFLite）。citeturn2search4  
- Android 13 通知权限。citeturn3search1  
- Android 14 选择性照片访问。citeturn11search4  
- Google Play target API 要求（2025）。citeturn12search0
