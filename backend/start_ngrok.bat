@echo off
chcp 65001 >nul
echo ========================================
echo 🌐 启动 ngrok 内网穿透
echo ========================================
echo.

REM 检查 ngrok 是否安装
ngrok version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未检测到 ngrok
    echo.
    echo 请按以下步骤安装:
    echo 1. 访问 https://ngrok.com/download
    echo 2. 下载 Windows 版本
    echo 3. 解压到任意目录
    echo 4. 将 ngrok.exe 所在目录添加到系统 PATH
    echo.
    echo 或者直接在 ngrok.exe 所在目录运行:
    echo    ngrok http 5000
    echo.
    pause
    exit /b 1
)

echo ✅ ngrok 已安装
echo.
echo 📡 启动 ngrok (端口 5000)...
echo.
echo ⚠️  重要提示:
echo    1. 复制下方显示的 Forwarding 地址 (https://xxx.ngrok.io)
echo    2. 在 Android 项目中更新 RetrofitClient.kt 的 BASE_URL
echo.
echo ========================================
echo.

ngrok http 5000

pause

