# 🚀 Cathub 后端部署到 Render 指南

## 📋 准备工作

### 1. 注册 Render 账号

1. 访问：https://render.com
2. 点击 **Sign Up**
3. 使用 GitHub 账号登录（推荐）或邮箱注册

### 2. 准备 GitHub 仓库

您需要将代码上传到 GitHub，Render 会从 GitHub 自动部署。

---

## 🔧 步骤 1：创建 GitHub 仓库

### 方法 1：使用 GitHub Desktop（推荐，简单）

1. **下载并安装 GitHub Desktop**
   - 访问：https://desktop.github.com
   - 下载并安装

2. **登录 GitHub 账号**
   - 打开 GitHub Desktop
   - File → Options → Accounts → Sign in

3. **创建仓库**
   - File → Add Local Repository
   - 选择 `d:\Desktop\cat_server`
   - 如果提示"不是 Git 仓库"，点击 **Create a repository**
   - Repository name: `cathub-server`
   - 点击 **Create Repository**

4. **提交代码**
   - 在左侧看到所有文件
   - 在 Summary 输入：`Initial commit`
   - 点击 **Commit to main**

5. **发布到 GitHub**
   - 点击顶部的 **Publish repository**
   - 取消勾选 "Keep this code private"（或保持勾选，都可以）
   - 点击 **Publish Repository**

### 方法 2：使用命令行

```powershell
# 进入项目目录
cd d:\Desktop\cat_server

# 初始化 Git 仓库
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit"

# 在 GitHub 网站上创建新仓库（cathub-server）
# 然后运行以下命令（替换 YOUR_USERNAME）
git remote add origin https://github.com/YOUR_USERNAME/cathub-server.git
git branch -M main
git push -u origin main
```

---

## 🚀 步骤 2：在 Render 上部署

### 1. 创建新的 Web Service

1. **登录 Render**：https://dashboard.render.com

2. **点击 "New +"** → 选择 **"Web Service"**

3. **连接 GitHub 仓库**
   - 如果是第一次，点击 **"Connect GitHub"**
   - 授权 Render 访问您的 GitHub
   - 选择 `cathub-server` 仓库
   - 点击 **Connect**

### 2. 配置 Web Service

填写以下信息：

| 字段 | 值 |
|------|-----|
| **Name** | `cathub-backend`（或任意名称） |
| **Region** | `Singapore`（新加坡，离中国最近） |
| **Branch** | `main` |
| **Root Directory** | `backend` |
| **Runtime** | `Python 3` |
| **Build Command** | `pip install -r requirements.txt` |
| **Start Command** | `gunicorn --bind 0.0.0.0:$PORT server:app` |
| **Instance Type** | `Free` |

### 3. 环境变量（可选）

点击 **Advanced** → **Add Environment Variable**：

| Key | Value |
|-----|-------|
| `PYTHON_VERSION` | `3.11.0` |
| `FLASK_ENV` | `production` |

### 4. 部署

1. 点击底部的 **"Create Web Service"**
2. Render 会自动开始部署
3. 等待 3-5 分钟

### 5. 查看部署状态

- 在 Logs 标签页可以看到部署日志
- 等待看到 `Your service is live 🎉`

### 6. 获取 URL

部署成功后，在页面顶部会显示您的服务 URL：

```
https://cathub-backend-xxxx.onrender.com
```

**复制这个 URL！** 这就是您的后端 API 地址。

---

## 📱 步骤 3：更新 Android App 配置

### 1. 修改 RetrofitClient.kt

打开文件：
```
android/app/src/main/java/com/cathub/app/data/api/RetrofitClient.kt
```

修改第 17 行：

```kotlin
private const val BASE_URL = "https://cathub-backend-xxxx.onrender.com/"
```

**注意**：
- ✅ 使用您刚才复制的 Render URL
- ✅ 末尾必须有 `/`
- ✅ 使用 `https://`（不是 `http://`）

### 2. 重新编译安装 App

```powershell
cd d:\Desktop\cat_server\android
.\gradlew.bat installDebug
```

---

## 🧪 步骤 4：测试

### 1. 测试后端 API

在浏览器中访问：
```
https://cathub-backend-xxxx.onrender.com/api/health
```

应该看到：
```json
{
  "status": "ok",
  "message": "Cathub API is running"
}
```

### 2. 测试 App

1. 打开手机上的 Cathub App
2. 点击"档案"
3. 应该能正常加载（第一次可能需要等待 60 秒，因为服务器在唤醒）

---

## 🔄 后续更新

### 每次修改代码后

1. **提交到 GitHub**（使用 GitHub Desktop 或命令行）
   ```powershell
   git add .
   git commit -m "更新说明"
   git push
   ```

2. **Render 会自动重新部署**
   - 无需手动操作
   - 等待 3-5 分钟

---

## ⚠️ 重要提示

### 免费版限制

- ✅ **完全免费**
- ⏱️ **15 分钟无请求后休眠**
- ⏰ **唤醒需要 30-60 秒**
- 💾 **数据永久保存**
- 📊 **750 小时/月运行时间**

### 数据持久化

**重要**：Render 免费版的文件系统是临时的，每次部署会重置。

**解决方案**：
- 使用 Render 的 PostgreSQL 数据库（免费）
- 或使用外部数据库服务

**当前状态**：
- SQLite 数据库会在每次部署后重置
- 上传的照片也会丢失

**如果需要持久化数据**，请告诉我，我会帮您配置 PostgreSQL。

---

## 🎉 完成！

现在您的后端已经部署到云端，可以：
- ✅ 从任何地方访问
- ✅ 不需要保持电脑运行
- ✅ 获得 HTTPS 加密
- ✅ 自动部署更新

---

## 📞 需要帮助？

如果遇到问题：
1. 查看 Render 的 Logs 标签页
2. 检查部署日志中的错误信息
3. 确保 GitHub 仓库中的代码是最新的

---

## 🔗 相关链接

- Render 文档：https://render.com/docs
- GitHub Desktop：https://desktop.github.com
- Render Dashboard：https://dashboard.render.com

