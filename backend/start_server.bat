@echo off
chcp 65001 >nul
echo ========================================
echo 🐱 Cathub 后端服务器启动脚本
echo ========================================
echo.

REM 检查 Python 是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未检测到 Python
    echo 请先安装 Python: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo ✅ Python 已安装
echo.

REM 进入 backend 目录
cd /d "%~dp0"

REM 检查依赖是否安装
echo 📦 检查依赖...
pip show flask >nul 2>&1
if errorlevel 1 (
    echo 📥 正在安装依赖...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo ❌ 依赖安装失败
        pause
        exit /b 1
    )
)

echo ✅ 依赖已安装
echo.

REM 启动服务器
echo 🚀 启动服务器...
echo 服务器将在 http://127.0.0.1:5000 启动
echo 按 Ctrl+C 可以停止服务器
echo.
python server.py

pause

