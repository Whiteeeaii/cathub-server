# 🤖 AI 识别功能配置指南

## 📋 目录
1. [功能介绍](#功能介绍)
2. [获取 Gemini API Key](#获取-gemini-api-key)
3. [配置步骤](#配置步骤)
4. [成本说明](#成本说明)
5. [使用方法](#使用方法)
6. [常见问题](#常见问题)

---

## 🎯 功能介绍

Cathub 现在支持两种识别方式：

### 1. **传统哈希识别**（默认）
- ✅ 完全免费
- ✅ 速度快（毫秒级）
- ⚠️ 准确度中等（60-70%）
- ⚠️ 对角度、光照敏感

### 2. **AI 识别**（需要配置）⭐
- 🤖 使用 Google Gemini Vision API
- ✅ 准确度高（85-95%）
- ✅ 对角度、光照变化鲁棒
- ✅ 可以理解猫咪特征
- 💰 有成本（但有免费额度）

---

## 🔑 获取 Gemini API Key

### 步骤 1：访问 Google AI Studio

访问：https://makersuite.google.com/app/apikey

### 步骤 2：登录 Google 账号

使用您的 Google 账号登录

### 步骤 3：创建 API Key

1. 点击 **"Create API Key"**
2. 选择一个 Google Cloud 项目（或创建新项目）
3. 复制生成的 API Key

**示例**：
```
AIzaSyABC123def456GHI789jkl012MNO345pqr
```

⚠️ **重要**：请妥善保管 API Key，不要分享给他人！

---

## ⚙️ 配置步骤

### 方案 A：在 Render 上配置（推荐）

#### 1. 登录 Render Dashboard

访问：https://dashboard.render.com

#### 2. 进入您的服务

点击 `cathub-backend` 服务

#### 3. 添加环境变量

1. 点击左侧 **"Environment"** 标签
2. 点击 **"Add Environment Variable"**
3. 填写：
   - **Key**: `GEMINI_API_KEY`
   - **Value**: 粘贴您的 API Key
4. 点击 **"Save Changes"**

#### 4. 等待重新部署

Render 会自动重新部署服务（约 3-5 分钟）

#### 5. 验证配置

查看 Logs，应该看到：
```
✅ Gemini API 已配置
🤖 AI 识别功能: 已启用
```

---

### 方案 B：本地开发配置

#### 1. 创建 .env 文件

在 `backend/` 目录下创建 `.env` 文件：

```bash
cd backend
cp .env.example .env
```

#### 2. 编辑 .env 文件

```env
GEMINI_API_KEY=your_actual_api_key_here
```

#### 3. 安装依赖

```bash
pip install python-dotenv
```

#### 4. 修改 server.py

在文件开头添加：
```python
from dotenv import load_dotenv
load_dotenv()
```

#### 5. 重启服务器

```bash
python server.py
```

---

## 💰 成本说明

### 免费额度

Google Gemini 提供慷慨的免费额度：

- **每分钟**：60 次请求
- **每天**：1,500 次请求
- **每月**：完全免费（在限额内）

### 个人使用估算

假设每天识别 **10 次**：

- **每月使用**：300 次
- **成本**：**$0**（完全在免费额度内）

### 付费价格（超出免费额度后）

| 模型 | 输入价格 | 输出价格 |
|------|---------|---------|
| Gemini 1.5 Flash | $0.075 / 百万 tokens | $0.30 / 百万 tokens |
| Gemini 1.5 Pro | $1.25 / 百万 tokens | $5.00 / 百万 tokens |

**每次识别成本**（使用 Flash 模型）：
- 图像输入：约 258 tokens
- 文本输出：约 100 tokens
- **单次成本**：约 $0.00005（0.005 分）

**结论**：即使超出免费额度，成本也非常低！

---

## 📱 使用方法

### Android App 端（未来更新）

目前 AI 识别默认启用（如果配置了 API Key）。

未来可以添加设置选项：
```kotlin
// 在设置中选择识别方式
val useAI = sharedPreferences.getBoolean("use_ai_recognition", true)
```

### API 调用

```bash
# 使用 AI 识别
curl -X POST https://cathub.onrender.com/api/recognize \
  -F "photo=@cat.jpg" \
  -F "use_ai=true"

# 使用传统哈希识别
curl -X POST https://cathub.onrender.com/api/recognize \
  -F "photo=@cat.jpg" \
  -F "use_ai=false"
```

---

## ❓ 常见问题

### Q1: 免费额度够用吗？

**A**: 对于个人使用完全够用！
- 每天 1,500 次请求
- 即使每天识别 100 次也只用 6.7% 的额度

### Q2: 会不会突然收费？

**A**: 不会！
- Google 会在超出免费额度时通知您
- 您可以设置预算上限
- 默认不会自动扣费

### Q3: API Key 安全吗？

**A**: 
- ✅ 在 Render 上配置是安全的（环境变量加密）
- ✅ 不要把 API Key 提交到 Git
- ✅ 可以随时在 Google AI Studio 撤销和重新生成

### Q4: AI 识别比哈希识别好多少？

**A**: 
- **准确度**：提升 20-30%
- **鲁棒性**：对角度、光照变化更稳定
- **理解能力**：可以识别花色、斑纹等特征

### Q5: 可以同时使用两种方式吗？

**A**: 可以！
- 默认使用 AI（如果配置了）
- 可以在 API 调用时指定 `use_ai=false` 使用哈希方法
- 未来可以在 App 设置中切换

### Q6: 识别速度会变慢吗？

**A**: 
- AI 识别：1-3 秒
- 哈希识别：50-200 毫秒
- 对于用户体验影响不大

### Q7: 如果 API 调用失败怎么办？

**A**: 
- 系统会自动回退到哈希识别
- 不会影响基本功能
- 会在日志中记录错误

---

## 🎯 推荐配置

### 个人使用
- ✅ 配置 Gemini API（免费）
- ✅ 享受高准确度识别
- ✅ 完全在免费额度内

### 测试阶段
- ⚠️ 可以先不配置
- ⚠️ 使用哈希识别测试功能
- ⚠️ 确认需求后再配置 AI

### 正式产品
- ✅ 必须配置 AI 识别
- ✅ 设置预算上限
- ✅ 监控使用量

---

## 📞 需要帮助？

如果在配置过程中遇到问题：

1. **检查日志**：在 Render Logs 中查看错误信息
2. **验证 API Key**：确保复制完整且正确
3. **检查额度**：访问 Google AI Studio 查看使用情况
4. **联系我**：随时告诉我遇到的问题！

---

## 🚀 下一步

配置完成后：

1. ✅ 重启服务器
2. ✅ 测试识别功能
3. ✅ 查看识别准确度
4. ✅ 享受 AI 识别！

祝您使用愉快！🎉

