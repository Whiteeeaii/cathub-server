"""
AI 识别模块 - 支持多种 AI 服务
- Google Gemini (国外)
- 阿里云通义千问 (国内推荐)
- 百度文心一言 (国内)
"""
import os
import json
import base64
from PIL import Image

# 检测使用哪个 AI 服务
AI_PROVIDER = os.environ.get('AI_PROVIDER', 'gemini').lower()  # gemini, qwen, ernie
GEMINI_API_KEY = os.environ.get('GEMINI_API_KEY', '')
QWEN_API_KEY = os.environ.get('DASHSCOPE_API_KEY', '')  # 阿里云通义千问
ERNIE_API_KEY = os.environ.get('ERNIE_API_KEY', '')  # 百度文心一言

model = None
ai_service = None

# 配置 Gemini
if AI_PROVIDER == 'gemini' and GEMINI_API_KEY:
    try:
        import google.generativeai as genai
        genai.configure(api_key=GEMINI_API_KEY)
        model = genai.GenerativeModel('gemini-1.5-flash')
        ai_service = 'gemini'
        print("✅ Google Gemini API 已配置")
    except Exception as e:
        print(f"❌ Gemini 配置失败: {str(e)}")

# 配置阿里云通义千问
elif AI_PROVIDER == 'qwen' and QWEN_API_KEY:
    try:
        import dashscope
        dashscope.api_key = QWEN_API_KEY
        ai_service = 'qwen'
        print("✅ 阿里云通义千问 API 已配置")
    except Exception as e:
        print(f"❌ 通义千问配置失败: {str(e)}")

# 配置百度文心一言
elif AI_PROVIDER == 'ernie' and ERNIE_API_KEY:
    try:
        import requests
        ai_service = 'ernie'
        print("✅ 百度文心一言 API 已配置")
    except Exception as e:
        print(f"❌ 文心一言配置失败: {str(e)}")

else:
    print(f"⚠️ 未配置 AI API Key，AI 识别功能不可用")
    print(f"   当前 AI_PROVIDER: {AI_PROVIDER}")
    print(f"   支持的服务: gemini (国外), qwen (阿里云), ernie (百度)")

def encode_image_base64(image_path):
    """将图片编码为 base64"""
    with open(image_path, 'rb') as f:
        return base64.b64encode(f.read()).decode('utf-8')

def describe_cat_features(image_path):
    """
    使用 AI 描述猫咪特征
    返回结构化的特征描述
    """
    if not ai_service:
        return None

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

    try:
        if ai_service == 'gemini':
            return _describe_with_gemini(image_path, prompt)
        elif ai_service == 'qwen':
            return _describe_with_qwen(image_path, prompt)
        elif ai_service == 'ernie':
            return _describe_with_ernie(image_path, prompt)
    except Exception as e:
        print(f"❌ AI 特征提取失败: {str(e)}")
        return None

def _describe_with_gemini(image_path, prompt):
    """使用 Gemini 描述"""
    img = Image.open(image_path)
    response = model.generate_content([prompt, img])
    text = response.text.strip()

    # 移除 markdown 代码块标记
    if text.startswith('```json'):
        text = text[7:]
    if text.startswith('```'):
        text = text[3:]
    if text.endswith('```'):
        text = text[:-3]
    text = text.strip()

    features = json.loads(text)
    print(f"✅ Gemini 特征提取成功: {features.get('overall_description', '')}")
    return features

def _describe_with_qwen(image_path, prompt, max_retries=3):
    """使用阿里云通义千问描述

    Args:
        image_path: 图片路径
        prompt: 提示词
        max_retries: 最大重试次数（默认 3 次）
    """
    from dashscope import MultiModalConversation
    import time

    # 读取图片并转为 base64
    image_base64 = encode_image_base64(image_path)

    messages = [{
        'role': 'user',
        'content': [
            {'image': f'data:image/jpeg;base64,{image_base64}'},
            {'text': prompt}
        ]
    }]

    # 重试机制
    for attempt in range(max_retries + 1):
        try:
            print(f"🤖 调用通义千问 API (尝试 {attempt + 1}/{max_retries + 1})...")
            start_time = time.time()

            response = MultiModalConversation.call(
                model='qwen-vl-turbo',  # 使用 qwen-vl-turbo（速度优先，3-8 秒响应）
                messages=messages,
                timeout=60  # 减少到 60 秒超时（turbo 模型更快）
            )

            elapsed = time.time() - start_time
            print(f"⏱️ API 响应时间: {elapsed:.2f} 秒")

            if response.status_code == 200:
                break
            else:
                print(f"⚠️ API 返回错误状态码: {response.status_code}")
                if attempt < max_retries:
                    print(f"🔄 等待 2 秒后重试...")
                    time.sleep(2)
                    continue
                else:
                    raise Exception(f"API 调用失败: {response.status_code}")

        except Exception as e:
            if attempt < max_retries:
                print(f"⚠️ API 调用失败: {str(e)}, 等待 2 秒后重试...")
                time.sleep(2)
                continue
            else:
                raise

    if response.status_code == 200:
        text = response.output.choices[0].message.content[0]['text'].strip()

        # 移除 markdown 代码块标记
        if text.startswith('```json'):
            text = text[7:]
        if text.startswith('```'):
            text = text[3:]
        if text.endswith('```'):
            text = text[:-3]
        text = text.strip()

        features = json.loads(text)
        print(f"✅ 通义千问特征提取成功: {features.get('overall_description', '')}")
        return features
    else:
        print(f"❌ 通义千问调用失败: {response.message}")
        return None

def _describe_with_ernie(image_path, prompt):
    """使用百度文心一言描述"""
    # TODO: 实现百度文心一言接口
    print("⚠️ 百度文心一言接口待实现")
    return None

def compare_cat_images(image1_path, image2_path):
    """
    使用 AI 比较两张猫咪照片
    返回相似度和判断理由
    """
    if not ai_service:
        return None

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

    try:
        if ai_service == 'gemini':
            return _compare_with_gemini(image1_path, image2_path, prompt)
        elif ai_service == 'qwen':
            return _compare_with_qwen(image1_path, image2_path, prompt)
        elif ai_service == 'ernie':
            return _compare_with_ernie(image1_path, image2_path, prompt)
    except Exception as e:
        print(f"❌ AI 比较失败: {str(e)}")
        return None

def _compare_with_gemini(image1_path, image2_path, prompt):
    """使用 Gemini 比较"""
    img1 = Image.open(image1_path)
    img2 = Image.open(image2_path)
    response = model.generate_content([prompt, img1, img2])

    text = response.text.strip()
    if text.startswith('```json'):
        text = text[7:]
    if text.startswith('```'):
        text = text[3:]
    if text.endswith('```'):
        text = text[:-3]
    text = text.strip()

    result = json.loads(text)
    print(f"✅ Gemini 比较完成: 相似度 {result.get('similarity', 0)}%")
    return result

def _compare_with_qwen(image1_path, image2_path, prompt, max_retries=3):
    """使用阿里云通义千问比较

    Args:
        image1_path: 第一张图片路径
        image2_path: 第二张图片路径
        prompt: 提示词
        max_retries: 最大重试次数（默认 3 次）
    """
    from dashscope import MultiModalConversation
    import time

    image1_base64 = encode_image_base64(image1_path)
    image2_base64 = encode_image_base64(image2_path)

    messages = [{
        'role': 'user',
        'content': [
            {'image': f'data:image/jpeg;base64,{image1_base64}'},
            {'image': f'data:image/jpeg;base64,{image2_base64}'},
            {'text': prompt}
        ]
    }]

    # 重试机制
    for attempt in range(max_retries + 1):
        try:
            print(f"🤖 调用通义千问比较 API (尝试 {attempt + 1}/{max_retries + 1})...")
            start_time = time.time()

            response = MultiModalConversation.call(
                model='qwen-vl-turbo',  # 使用 qwen-vl-turbo（速度优先，3-8 秒响应）
                messages=messages,
                timeout=60  # 减少到 60 秒超时（turbo 模型更快）
            )

            elapsed = time.time() - start_time
            print(f"⏱️ API 响应时间: {elapsed:.2f} 秒")

            if response.status_code == 200:
                break
            else:
                print(f"⚠️ API 返回错误状态码: {response.status_code}")
                if attempt < max_retries:
                    print(f"🔄 等待 2 秒后重试...")
                    time.sleep(2)
                    continue
                else:
                    raise Exception(f"API 调用失败: {response.status_code}")

        except Exception as e:
            if attempt < max_retries:
                print(f"⚠️ API 调用失败: {str(e)}, 等待 2 秒后重试...")
                time.sleep(2)
                continue
            else:
                raise

    if response.status_code == 200:
        text = response.output.choices[0].message.content[0]['text'].strip()

        if text.startswith('```json'):
            text = text[7:]
        if text.startswith('```'):
            text = text[3:]
        if text.endswith('```'):
            text = text[:-3]
        text = text.strip()

        result = json.loads(text)
        print(f"✅ 通义千问比较完成: 相似度 {result.get('similarity', 0)}%")
        return result
    else:
        print(f"❌ 通义千问调用失败: {response.message}")
        return None

def _compare_with_ernie(image1_path, image2_path, prompt):
    """使用百度文心一言比较"""
    # TODO: 实现百度文心一言接口
    print("⚠️ 百度文心一言接口待实现")
    return None

def recognize_cat_from_database(upload_image_path, cats_data):
    """
    使用 AI 从数据库中识别猫咪

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
    if not ai_service:
        print("❌ AI 服务未配置")
        return []

    print(f"🤖 开始 AI 识别 (服务商: {ai_service})")
    
    try:
        # 1. 描述上传的猫咪
        print(f"📸 分析上传的照片: {upload_image_path}")
        upload_features = describe_cat_features(upload_image_path)
        if not upload_features:
            print("❌ 无法提取上传照片的特征")
            return []

        print(f"✅ 上传照片特征: {upload_features.get('overall_description', '')}")

        matches = []

        # 2. 与每只猫咪的照片比较
        print(f"🔍 开始与 {len(cats_data)} 只猫咪比较...")
        for cat in cats_data:
            if not cat.get('photos'):
                print(f"  ⚠️ {cat.get('name', 'Unknown')} 没有照片，跳过")
                continue

            max_similarity = 0
            best_reason = ""

            print(f"  📷 比较猫咪: {cat.get('name', 'Unknown')} ({len(cat['photos'])} 张照片)")

            # 与该猫咪的每张照片比较
            for i, photo in enumerate(cat['photos']):
                photo_path = photo.get('path')
                if not photo_path:
                    print(f"    ⚠️ 照片 {i+1} 没有路径")
                    continue
                if not os.path.exists(photo_path):
                    print(f"    ⚠️ 照片 {i+1} 不存在: {photo_path}")
                    continue

                print(f"    🔄 比较照片 {i+1}: {photo_path}")

                # 使用 AI 比较
                result = compare_cat_images(upload_image_path, photo_path)
                if result:
                    similarity = result.get('similarity', 0)
                    print(f"    ✅ 相似度: {similarity}%")
                    if similarity > max_similarity:
                        max_similarity = similarity
                        best_reason = result.get('reason', '')
                else:
                    print(f"    ❌ 比较失败")

            # 如果相似度超过阈值，添加到匹配列表
            if max_similarity > 50:  # 50% 阈值
                print(f"  ✅ 匹配成功: {cat.get('name', 'Unknown')} (相似度: {max_similarity}%)")
                matches.append({
                    'cat': cat,
                    'similarity': max_similarity,
                    'reason': best_reason
                })
            else:
                print(f"  ❌ 相似度不足: {cat.get('name', 'Unknown')} (相似度: {max_similarity}%)")
        
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
    return ai_service is not None

def get_ai_provider():
    """获取当前使用的 AI 服务商"""
    return ai_service

