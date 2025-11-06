#!/usr/bin/env python3
"""
生成 Android 应用图标
将 cathublogo.png 转换为不同尺寸的应用图标
"""

from PIL import Image
import os

# 图标尺寸配置
ICON_SIZES = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

def generate_icons(source_image_path, output_base_dir):
    """生成不同尺寸的应用图标"""
    print(f"📸 读取源图片: {source_image_path}")
    
    # 打开源图片
    img = Image.open(source_image_path)
    print(f"✅ 源图片尺寸: {img.size}")
    
    # 确保图片是 RGBA 模式（支持透明度）
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    
    # 为每个尺寸生成图标
    for folder, size in ICON_SIZES.items():
        output_dir = os.path.join(output_base_dir, folder)
        os.makedirs(output_dir, exist_ok=True)
        
        # 调整图片大小
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # 保存图标
        output_path = os.path.join(output_dir, 'ic_launcher.png')
        resized.save(output_path, 'PNG')
        print(f"✅ 生成 {folder}/ic_launcher.png ({size}x{size})")
        
        # 同时生成圆形图标
        output_path_round = os.path.join(output_dir, 'ic_launcher_round.png')
        resized.save(output_path_round, 'PNG')
        print(f"✅ 生成 {folder}/ic_launcher_round.png ({size}x{size})")
    
    print("🎉 所有图标生成完成！")

if __name__ == '__main__':
    source_image = 'images/cathublogo.png'
    output_dir = 'android/app/src/main/res'
    
    if not os.path.exists(source_image):
        print(f"❌ 源图片不存在: {source_image}")
        exit(1)
    
    generate_icons(source_image, output_dir)

