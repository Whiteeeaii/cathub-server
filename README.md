# Cathub - 流浪猫管理 App

基于 Android + Python Flask 的流浪猫档案管理与识别系统。

## 📁 项目结构

```
cat_server/
├── backend/              # Python Flask 后端
│   ├── server.py        # API 服务器
│   ├── requirements.txt # Python 依赖
│   ├── download_models.py # ML 模型下载脚本
│   ├── models/          # TensorFlow Lite 模型文件
│   ├── uploads/         # 上传的照片
│   └── cathub.db        # SQLite 数据库
│
└── android/             # Android App
    ├── app/
    │   └── src/main/
    │       ├── java/com/cathub/app/
    │       │   ├── data/        # 数据层（API、Model、Repository）
    │       │   ├── ui/          # UI 层（Compose）
    │       │   ├── navigation/  # 导航
    │       │   └── MainActivity.kt
    │       ├── res/             # 资源文件
    │       └── AndroidManifest.xml
    └── build.gradle.kts
```

## 🚀 快速开始

### 第一步：启动后端服务器

#### 1. 安装 Python（如果还没有）

**Windows 系统：**
1. 访问 https://www.python.org/downloads/
2. 下载 Python 3.10 或更高版本
3. 安装时勾选 "Add Python to PATH"

验证安装：
```powershell
python --version
```

#### 2. 安装依赖

在项目根目录打开 PowerShell：

```powershell
cd backend
pip install -r requirements.txt
```

#### 3. 启动服务器

```powershell
python server.py
```

看到以下输出表示成功：
```
==================================================
🐱 Cathub 后端服务器启动中...
==================================================
📡 API 地址: http://localhost:5000
...
```

### 第二步：配置内网穿透（ngrok）

#### 1. 下载 ngrok

访问 https://ngrok.com/download 下载 Windows 版本

#### 2. 注册账号（免费）

访问 https://dashboard.ngrok.com/signup 注册

#### 3. 配置 authtoken

在 ngrok 控制台复制您的 authtoken，然后运行：

```powershell
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

#### 4. 启动 ngrok

**新开一个 PowerShell 窗口**（保持后端服务器运行），运行：

```powershell
ngrok http 5000
```

看到类似输出：
```
Forwarding  https://abc123.ngrok.io -> http://localhost:5000
```

**复制这个 https://abc123.ngrok.io 地址！**

### 第三步：配置 Android App

#### 1. 打开 Android Studio

导入 `android` 文件夹作为项目

#### 2. 配置服务器地址

打开文件：`android/app/src/main/java/com/cathub/app/data/api/RetrofitClient.kt`

修改第 17 行：
```kotlin
private const val BASE_URL = "https://abc123.ngrok.io/" // 替换为您的 ngrok 地址
```

#### 3. 同步 Gradle

点击 Android Studio 顶部的 "Sync Now"

#### 4. 运行 App

- 连接 Android 手机（开启 USB 调试）
- 或使用 Android 模拟器
- 点击 "Run" 按钮

## 📱 功能说明

### 已实现功能 ✅

1. **主页三分栏**
   - 识别入口
   - 上报入口
   - 档案入口

2. **猫咪档案管理**
   - 添加猫咪档案（名字、性别、年龄、花色、活动区域、性格、食物喜好、投喂建议）
   - 查看档案列表
   - 查看档案详情

3. **后端 API**
   - 猫咪档案 CRUD
   - 目击记录
   - 健康上报
   - 投喂记录

4. **极简线框 UI**
   - 纯白背景
   - 单像素线框
   - Material3 定制主题

### 待实现功能 🚧

1. **端侧识别**（下一步）
   - CameraX 相机预览
   - TensorFlow Lite 目标检测
   - 图像嵌入与向量检索
   - 匹配结果展示

2. **状态上报**
   - 健康上报表单
   - 照片上传
   - 上报历史

3. **照片管理**
   - 拍照/选择照片
   - 照片上传到服务器
   - 多样本管理

## 🔧 开发指南

### 测试 API

使用浏览器或 Postman 测试：

```
# 健康检查
GET http://localhost:5000/api/health

# 获取所有猫咪
GET http://localhost:5000/api/cats

# 创建猫咪
POST http://localhost:5000/api/cats
Content-Type: application/json

{
  "name": "雪球",
  "sex": "female",
  "age_months": 18,
  "pattern": "三花",
  "activity_areas": ["小区东门", "停车场"],
  "personality": ["温顺", "胆小"],
  "food_preferences": ["鸡胸肉", "幼猫粮"],
  "feeding_tips": "避免乳制品；少量多餐"
}
```

### 查看数据库

使用 SQLite 浏览器（如 DB Browser for SQLite）打开 `backend/cathub.db`

### 修改 UI

所有 UI 代码在 `android/app/src/main/java/com/cathub/app/ui/` 目录

### 添加新 API

1. 在 `backend/server.py` 添加路由
2. 在 `android/app/src/main/java/com/cathub/app/data/api/CathubApi.kt` 添加接口
3. 在 Repository 中调用

## 📦 ML 模型

已下载的模型文件位于 `backend/models/`：

1. **efficientdet_lite0.tflite** (13.2 MB)
   - 目标检测模型
   - 用于检测画面中的猫

2. **mobilenet_v3_small.tflite** (3.9 MB)
   - 图像嵌入模型
   - 用于生成猫咪特征向量

### 将模型集成到 Android

将模型文件复制到：
```
android/app/src/main/assets/
```

## 🐛 常见问题

### 1. Android App 无法连接服务器

- 确保后端服务器正在运行
- 确保 ngrok 正在运行
- 检查 `RetrofitClient.kt` 中的 BASE_URL 是否正确
- 查看 Android Studio 的 Logcat 日志

### 2. ngrok 地址每次都变

免费版 ngrok 每次重启地址会变，需要：
- 每次重启后更新 `RetrofitClient.kt` 中的地址
- 或升级到 ngrok 付费版获得固定域名

### 3. Python 依赖安装失败

尝试使用国内镜像：
```powershell
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

### 4. Android Gradle 同步失败

- 检查网络连接
- 使用 VPN（如果在国内）
- 或配置 Gradle 镜像

## 📝 下一步开发计划

1. ✅ 基础架构搭建
2. ✅ 档案管理功能
3. 🚧 端侧识别功能（CameraX + TFLite）
4. 🚧 状态上报功能
5. 🚧 照片上传功能
6. 🚧 投喂记录功能

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**开发者：** Cathub Team  
**版本：** v1.0.0  
**更新日期：** 2025-11-06

