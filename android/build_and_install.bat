@echo off
chcp 65001 >nul
echo ========================================
echo 📱 Cathub Android App 编译和安装
echo ========================================
echo.

echo 请选择操作:
echo 1. 编译 Debug APK
echo 2. 编译并安装到手机 (需要连接手机)
echo 3. 仅安装已编译的 APK
echo 4. 查看连接的设备
echo 5. 退出
echo.

set /p choice="请输入选项 (1-5): "

if "%choice%"=="1" goto build_debug
if "%choice%"=="2" goto build_and_install
if "%choice%"=="3" goto install_only
if "%choice%"=="4" goto list_devices
if "%choice%"=="5" goto end

echo ❌ 无效选项
goto end

:build_debug
echo.
echo 🔨 开始编译 Debug APK...
echo.
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo.
    echo ❌ 编译失败
    pause
    goto end
)
echo.
echo ✅ 编译成功！
echo 📦 APK 位置: app\build\outputs\apk\debug\app-debug.apk
echo.
pause
goto end

:build_and_install
echo.
echo 🔨 开始编译并安装...
echo.

REM 检查设备连接
adb devices | findstr "device$" >nul
if errorlevel 1 (
    echo ❌ 未检测到设备
    echo.
    echo 请确保:
    echo 1. 手机已用 USB 连接到电脑
    echo 2. 手机已开启 USB 调试
    echo 3. 手机上已授权此电脑进行调试
    echo.
    pause
    goto end
)

echo ✅ 检测到设备
echo.

call gradlew.bat installDebug
if errorlevel 1 (
    echo.
    echo ❌ 安装失败
    pause
    goto end
)
echo.
echo ✅ 安装成功！
echo 🎉 您可以在手机上打开 Cathub App 了
echo.
pause
goto end

:install_only
echo.
echo 📱 安装 APK 到手机...
echo.

REM 检查 APK 是否存在
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ❌ 未找到 APK 文件
    echo 请先编译 APK (选项 1 或 2)
    echo.
    pause
    goto end
)

REM 检查设备连接
adb devices | findstr "device$" >nul
if errorlevel 1 (
    echo ❌ 未检测到设备
    echo.
    echo 请确保:
    echo 1. 手机已用 USB 连接到电脑
    echo 2. 手机已开启 USB 调试
    echo 3. 手机上已授权此电脑进行调试
    echo.
    pause
    goto end
)

echo ✅ 检测到设备
echo.

adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo.
    echo ❌ 安装失败
    pause
    goto end
)
echo.
echo ✅ 安装成功！
echo 🎉 您可以在手机上打开 Cathub App 了
echo.
pause
goto end

:list_devices
echo.
echo 📱 连接的设备列表:
echo.
adb devices
echo.
echo 如果列表为空或显示 "unauthorized":
echo 1. 检查 USB 连接
echo 2. 在手机上开启 USB 调试
echo 3. 在手机上授权此电脑
echo.
pause
goto end

:end

