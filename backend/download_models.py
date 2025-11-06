"""
下载 TensorFlow Lite 模型文件
"""
import urllib.request
import os

# 创建模型目录
os.makedirs('models', exist_ok=True)

print("=" * 60)
print("📥 开始下载 ML 模型文件...")
print("=" * 60)

# 模型 1: EfficientDet-Lite0 (目标检测)
print("\n1️⃣ 下载 EfficientDet-Lite0 目标检测模型...")
efficientdet_url = "https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/float32/latest/efficientdet_lite0.tflite"
efficientdet_path = "models/efficientdet_lite0.tflite"

try:
    print(f"   URL: {efficientdet_url}")
    print(f"   保存到: {efficientdet_path}")
    urllib.request.urlretrieve(efficientdet_url, efficientdet_path)
    size = os.path.getsize(efficientdet_path) / (1024 * 1024)
    print(f"   ✅ 下载成功! 文件大小: {size:.2f} MB")
except Exception as e:
    print(f"   ❌ 下载失败: {e}")
    print(f"   请手动下载: {efficientdet_url}")

# 模型 2: MobileNet V3 图像嵌入模型
print("\n2️⃣ 下载 MobileNet V3 图像嵌入模型...")
mobilenet_url = "https://storage.googleapis.com/mediapipe-models/image_embedder/mobilenet_v3_small/float32/latest/mobilenet_v3_small.tflite"
mobilenet_path = "models/mobilenet_v3_small.tflite"

try:
    print(f"   URL: {mobilenet_url}")
    print(f"   保存到: {mobilenet_path}")
    urllib.request.urlretrieve(mobilenet_url, mobilenet_path)
    size = os.path.getsize(mobilenet_path) / (1024 * 1024)
    print(f"   ✅ 下载成功! 文件大小: {size:.2f} MB")
except Exception as e:
    print(f"   ❌ 下载失败: {e}")
    print(f"   请手动下载: {mobilenet_url}")

print("\n" + "=" * 60)
print("✅ 模型下载完成!")
print("=" * 60)
print("\n📁 模型文件位置:")
print(f"   - {efficientdet_path}")
print(f"   - {mobilenet_path}")
print("\n💡 下一步: 将这些文件复制到 Android 项目的 assets 目录")
print("=" * 60)

