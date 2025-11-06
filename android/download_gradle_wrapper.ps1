# 下载 Gradle Wrapper JAR 文件

$url = "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle\wrapper\gradle-wrapper.jar"

Write-Host "正在下载 Gradle Wrapper JAR..." -ForegroundColor Green
Write-Host "URL: $url" -ForegroundColor Cyan
Write-Host "保存到: $output" -ForegroundColor Cyan

try {
    # 创建目录（如果不存在）
    $dir = Split-Path -Parent $output
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }

    # 下载文件
    Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
    
    Write-Host "`n✅ 下载成功！" -ForegroundColor Green
    Write-Host "文件大小: $((Get-Item $output).Length) 字节" -ForegroundColor Cyan
    
} catch {
    Write-Host "`n❌ 下载失败: $_" -ForegroundColor Red
    Write-Host "`n尝试备用地址..." -ForegroundColor Yellow
    
    # 备用地址
    $url2 = "https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar"
    
    try {
        Invoke-WebRequest -Uri $url2 -OutFile $output -UseBasicParsing
        Write-Host "✅ 使用备用地址下载成功！" -ForegroundColor Green
    } catch {
        Write-Host "❌ 备用地址也失败了" -ForegroundColor Red
        Write-Host "`n请手动下载:" -ForegroundColor Yellow
        Write-Host "1. 访问: https://services.gradle.org/distributions/gradle-8.9-bin.zip" -ForegroundColor Cyan
        Write-Host "2. 解压后，将 gradle-8.9/lib/gradle-wrapper.jar 复制到:" -ForegroundColor Cyan
        Write-Host "   $output" -ForegroundColor Cyan
        exit 1
    }
}

Write-Host "`n现在可以运行: .\gradlew.bat build" -ForegroundColor Green

