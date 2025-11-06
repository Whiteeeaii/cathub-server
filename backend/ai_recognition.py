"""
AI 识别模块 - 使用 Google Gemini API
"""
import os
import json
import google.generativeai as genai
from PIL import Image

# 配置 Gemini API
# 从环境变量读取 API Key
GEMINI_API_KEY = os.environ.get('GEMINI_API_KEY', '')

if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    model = genai.GenerativeModel('gemini-1.5-flash')
    print("✅ Gemini API 已配置")
else:
    model = None
    print("⚠️ 未配置 GEMINI_API_KEY，AI 识别功能不可用")

def describe_cat_features(image_path):
    """
    使用 Gemini 描述猫咪特征
    返回结构化的特征描述
    """
    if not model:
        return None
    
    try:
        img = Image.open(image_path)
        
        prompt = """
        请详细描述这只猫咪的特征。请用 JSON 格式返回，包含以下字段：
        
        {
            "pattern": "花色类型（如：三花、橘猫、黑白、狸花、纯色等）",
            "primary_color": "主要颜色",
            "markings": "斑纹分布描述",
            "body_type": "体型（瘦小/中等/壮实）",
            "distinctive_features": "显著特征列表",
            "overall_description": "整体描述（一句话）"
        }
        
        只返回 JSON，不要其他文字。
        """
        
        response = model.generate_content([prompt, img])
        
        # 解析 JSON
        text = response.text.strip()
        # 移除可能的 markdown 代码块标记
        if text.startswith('```json'):
            text = text[7:]
        if text.startswith('```'):
            text = text[3:]
        if text.endswith('```'):
            text = text[:-3]
        text = text.strip()
        
        features = json.loads(text)
        print(f"✅ AI 特征提取成功: {features.get('overall_description', '')}")
        return features
        
    except Exception as e:
        print(f"❌ AI 特征提取失败: {str(e)}")
        return None

def compare_cat_images(image1_path, image2_path):
    """
    使用 Gemini 比较两张猫咪照片
    返回相似度和判断理由
    """
    if not model:
        return None
    
    try:
        img1 = Image.open(image1_path)
        img2 = Image.open(image2_path)
        
        prompt = """
        请判断这两张照片是否是同一只猫。
        
        请从以下方面比较：
        1. 花色和斑纹图案是否一致
        2. 斑纹的位置和分布是否相同
        3. 体型是否相似
        4. 其他显著特征
        
        请用 JSON 格式返回：
        {
            "is_same_cat": true/false,
            "similarity": 0-100 的数字,
            "reason": "判断理由",
            "confidence": "high/medium/low"
        }
        
        只返回 JSON，不要其他文字。
        """
        
        response = model.generate_content([prompt, img1, img2])
        
        # 解析 JSON
        text = response.text.strip()
        if text.startswith('```json'):
            text = text[7:]
        if text.startswith('```'):
            text = text[3:]
        if text.endswith('```'):
            text = text[:-3]
        text = text.strip()
        
        result = json.loads(text)
        print(f"✅ AI 比较完成: 相似度 {result.get('similarity', 0)}%")
        return result
        
    except Exception as e:
        print(f"❌ AI 比较失败: {str(e)}")
        return None

def recognize_cat_from_database(upload_image_path, cats_data):
    """
    使用 Gemini 从数据库中识别猫咪
    
    参数:
        upload_image_path: 上传的照片路径
        cats_data: 数据库中的猫咪列表，格式：
            [
                {
                    "id": 1,
                    "name": "小花",
                    "pattern": "三花",
                    "photos": [{"path": "/path/to/photo.jpg"}]
                },
                ...
            ]
    
    返回:
        匹配的猫咪列表，按相似度排序
    """
    if not model:
        return []
    
    try:
        # 1. 描述上传的猫咪
        upload_features = describe_cat_features(upload_image_path)
        if not upload_features:
            return []
        
        matches = []
        
        # 2. 与每只猫咪的照片比较
        for cat in cats_data:
            if not cat.get('photos'):
                continue
            
            max_similarity = 0
            best_reason = ""
            
            # 与该猫咪的每张照片比较
            for photo in cat['photos']:
                photo_path = photo.get('path')
                if not photo_path or not os.path.exists(photo_path):
                    continue
                
                # 使用 AI 比较
                result = compare_cat_images(upload_image_path, photo_path)
                if result and result.get('similarity', 0) > max_similarity:
                    max_similarity = result['similarity']
                    best_reason = result.get('reason', '')
            
            # 如果相似度超过阈值，添加到匹配列表
            if max_similarity > 50:  # 50% 阈值
                matches.append({
                    'cat': cat,
                    'similarity': max_similarity,
                    'reason': best_reason
                })
        
        # 按相似度排序
        matches.sort(key=lambda x: x['similarity'], reverse=True)
        
        print(f"✅ AI 识别完成，找到 {len(matches)} 个匹配")
        return matches
        
    except Exception as e:
        print(f"❌ AI 识别失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return []

def is_ai_available():
    """检查 AI 功能是否可用"""
    return model is not None

