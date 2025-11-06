# 📱 Cathub Android App 安装指南

## 方法一：使用 Android Studio（最简单）⭐

### 步骤 1：准备手机

#### 开启开发者选项

1. 打开手机的 **设置**
2. 找到 **关于手机** 或 **关于设备**
3. 找到 **版本号** 或 **内部版本号**
4. **连续点击 7 次**，会提示"您已处于开发者模式"

#### 开启 USB 调试

1. 返回设置主页
2. 找到 **系统** 或 **更多设置**
3. 找到 **开发者选项**
4. 开启 **USB 调试**
5. （可选）开启 **USB 安装** 或 **通过 USB 验证应用**

#### 连接手机

1. 用 USB 数据线连接手机到电脑
2. 手机上会弹出 **允许 USB 调试** 的提示
3. 勾选 **始终允许使用这台计算机进行调试**
4. 点击 **允许** 或 **确定**

### 步骤 2：在 Android Studio 中运行

1. 打开 Android Studio
2. 打开 `android` 项目文件夹
3. 等待 Gradle 同步完成（首次可能需要几分钟）
4. 在顶部工具栏，设备下拉菜单中选择您的手机
5. 点击绿色的 **▶️ Run** 按钮
6. 等待编译和安装完成
7. App 会自动在手机上启动

**✅ 完成！App 已安装到手机**

---

## 方法二：使用命令行编译 APK

### 步骤 1：编译 APK

打开 PowerShell，进入 android 目录：

```powershell
cd android
```

#### 选项 A：使用自动化脚本（推荐）

双击运行：
```
android/build_and_install.bat
```

选择选项：
- **选项 1**：仅编译 APK
- **选项 2**：编译并自动安装到手机
- **选项 3**：安装已编译的 APK
- **选项 4**：查看连接的设备

#### 选项 B：手动编译

```powershell
# Windows
.\gradlew.bat assembleDebug

# Mac/Linux
./gradlew assembleDebug
```

编译完成后，APK 文件位于：
```
android\app\build\outputs\apk\debug\app-debug.apk
```

### 步骤 2：安装 APK

#### 方法 A：使用 ADB 安装（推荐）

确保手机已连接并开启 USB 调试，然后运行：

```powershell
# 检查设备是否连接
adb devices

# 安装 APK
adb install app\build\outputs\apk\debug\app-debug.apk

# 如果已安装，使用 -r 参数覆盖安装
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

#### 方法 B：手动传输安装

1. 将 `app-debug.apk` 文件复制到手机（通过 USB、蓝牙、微信等）
2. 在手机上打开文件管理器
3. 找到 APK 文件
4. 点击安装
5. 如果提示"禁止安装未知来源应用"，需要在设置中允许

---

## 方法三：生成可分享的 APK

如果您想把 APK 分享给其他人安装：

### 步骤 1：编译 APK

```powershell
cd android
.\gradlew.bat assembleDebug
```

### 步骤 2：找到 APK 文件

APK 位于：
```
android\app\build\outputs\apk\debug\app-debug.apk
```

### 步骤 3：分享 APK

将这个文件发送给其他人，他们可以：
1. 在手机上打开 APK 文件
2. 允许安装未知来源应用
3. 点击安装

---

## 🐛 常见问题

### Q1: 提示"未检测到设备"

**解决方案：**

1. **检查 USB 连接**
   - 确保使用数据线（不是仅充电线）
   - 尝试更换 USB 端口
   - 尝试更换数据线

2. **检查 USB 调试是否开启**
   - 设置 → 开发者选项 → USB 调试

3. **检查驱动程序**
   - Windows 可能需要安装手机驱动
   - 访问手机厂商官网下载驱动

4. **检查 ADB**
   ```powershell
   adb devices
   ```
   应该显示您的设备，如果显示 `unauthorized`，在手机上授权

### Q2: 编译失败

**解决方案：**

1. **清理项目**
   ```powershell
   cd android
   .\gradlew.bat clean
   .\gradlew.bat assembleDebug
   ```

2. **检查网络**
   - 首次编译需要下载依赖
   - 国内用户可能需要配置镜像或使用 VPN

3. **检查 Java 版本**
   ```powershell
   java -version
   ```
   需要 Java 17 或更高版本

### Q3: 安装失败

**解决方案：**

1. **卸载旧版本**
   ```powershell
   adb uninstall com.cathub.app
   ```
   然后重新安装

2. **检查存储空间**
   - 确保手机有足够的存储空间

3. **允许未知来源**
   - 设置 → 安全 → 允许安装未知应用

### Q4: App 闪退

**解决方案：**

1. **检查后端是否运行**
   - 确保 Flask 服务器正在运行
   - 确保 ngrok 正在运行

2. **检查网络配置**
   - 确保 `RetrofitClient.kt` 中的 BASE_URL 正确

3. **查看日志**
   ```powershell
   adb logcat | findstr "cathub"
   ```

### Q5: 手机上找不到开发者选项

**不同品牌手机的开启方法：**

- **小米/红米**：设置 → 我的设备 → 全部参数 → 连续点击 MIUI 版本
- **华为/荣耀**：设置 → 关于手机 → 连续点击版本号
- **OPPO/一加**：设置 → 关于手机 → 版本信息 → 连续点击版本号
- **vivo**：设置 → 系统管理 → 关于手机 → 连续点击软件版本号
- **三星**：设置 → 关于手机 → 软件信息 → 连续点击内部版本号

---

## 📋 完整流程总结

### 使用 Android Studio（推荐）

```
1. 开启手机 USB 调试
2. 连接手机到电脑
3. 在手机上授权调试
4. Android Studio 中点击 Run
5. 完成！
```

### 使用命令行

```powershell
# 1. 编译
cd android
.\gradlew.bat assembleDebug

# 2. 安装
adb install app\build\outputs\apk\debug\app-debug.apk

# 3. 完成！
```

### 使用自动化脚本（最简单）

```
1. 双击 android/build_and_install.bat
2. 选择选项 2（编译并安装）
3. 完成！
```

---

## 🎯 下一步

安装完成后：

1. **启动后端服务器**
   ```powershell
   双击 backend/start_server.bat
   ```

2. **启动 ngrok**
   ```powershell
   双击 backend/start_ngrok.bat
   ```

3. **配置服务器地址**
   - 如果还没配置，需要在 `RetrofitClient.kt` 中更新 ngrok 地址
   - 重新编译安装

4. **开始使用**
   - 打开 App
   - 点击"档案"
   - 添加第一只猫咪

---

## 💡 提示

### 开发时的工作流程

1. 修改代码
2. 在 Android Studio 中点击 Run（会自动编译并安装）
3. 在手机上测试
4. 重复

### 快速重新安装

如果只是修改了代码，不需要重新编译整个项目：

```powershell
# Android Studio 会自动增量编译
# 或使用命令行
cd android
.\gradlew.bat installDebug
```

### 查看实时日志

```powershell
# 查看所有日志
adb logcat

# 只查看 Cathub 相关日志
adb logcat | findstr "cathub"

# 清空日志
adb logcat -c
```

---

**祝您安装顺利！🐱**

