"""
测试 Cathub API
"""
import requests
import json

BASE_URL = "http://localhost:5000"

def test_health():
    """测试健康检查"""
    print("\n1️⃣ 测试健康检查...")
    response = requests.get(f"{BASE_URL}/api/health")
    print(f"   状态码: {response.status_code}")
    print(f"   响应: {response.json()}")
    assert response.status_code == 200

def test_create_cat():
    """测试创建猫咪"""
    print("\n2️⃣ 测试创建猫咪...")
    data = {
        "name": "雪球",
        "sex": "female",
        "age_months": 18,
        "pattern": "三花",
        "activity_areas": ["小区东门", "停车场"],
        "personality": ["温顺", "胆小"],
        "food_preferences": ["鸡胸肉", "幼猫粮"],
        "feeding_tips": "避免乳制品；少量多餐"
    }
    response = requests.post(f"{BASE_URL}/api/cats", json=data)
    print(f"   状态码: {response.status_code}")
    result = response.json()
    print(f"   响应: {result}")
    assert response.status_code == 201
    return result["id"]

def test_get_cats():
    """测试获取猫咪列表"""
    print("\n3️⃣ 测试获取猫咪列表...")
    response = requests.get(f"{BASE_URL}/api/cats")
    print(f"   状态码: {response.status_code}")
    cats = response.json()
    print(f"   猫咪数量: {len(cats)}")
    if cats:
        print(f"   第一只猫: {cats[0]['name']}")
    assert response.status_code == 200

def test_get_cat(cat_id):
    """测试获取单个猫咪"""
    print(f"\n4️⃣ 测试获取猫咪详情 (ID: {cat_id})...")
    response = requests.get(f"{BASE_URL}/api/cats/{cat_id}")
    print(f"   状态码: {response.status_code}")
    cat = response.json()
    print(f"   猫咪名字: {cat['name']}")
    print(f"   花色: {cat['pattern']}")
    print(f"   活动区域: {cat['activity_areas']}")
    assert response.status_code == 200

def test_create_sighting(cat_id):
    """测试创建目击记录"""
    print(f"\n5️⃣ 测试创建目击记录 (猫咪 ID: {cat_id})...")
    data = {
        "cat_id": cat_id,
        "location": "小区东门",
        "similarity": 0.95,
        "device": "测试设备",
        "reporter": "测试用户"
    }
    response = requests.post(f"{BASE_URL}/api/sightings", json=data)
    print(f"   状态码: {response.status_code}")
    print(f"   响应: {response.json()}")
    assert response.status_code == 201

def test_create_health_report(cat_id):
    """测试创建健康上报"""
    print(f"\n6️⃣ 测试创建健康上报 (猫咪 ID: {cat_id})...")
    data = {
        "cat_id": cat_id,
        "type": "injury",
        "severity": "medium",
        "note": "左前腿有轻微擦伤",
        "reporter": "测试用户"
    }
    response = requests.post(f"{BASE_URL}/api/health_reports", json=data)
    print(f"   状态码: {response.status_code}")
    print(f"   响应: {response.json()}")
    assert response.status_code == 201

def test_create_feed_log(cat_id):
    """测试创建投喂记录"""
    print(f"\n7️⃣ 测试创建投喂记录 (猫咪 ID: {cat_id})...")
    data = {
        "cat_id": cat_id,
        "food": "鸡胸肉",
        "qty": "50g",
        "note": "吃得很开心",
        "reporter": "测试用户"
    }
    response = requests.post(f"{BASE_URL}/api/feed_logs", json=data)
    print(f"   状态码: {response.status_code}")
    print(f"   响应: {response.json()}")
    assert response.status_code == 201

if __name__ == "__main__":
    print("=" * 60)
    print("🧪 Cathub API 测试")
    print("=" * 60)
    
    try:
        test_health()
        cat_id = test_create_cat()
        test_get_cats()
        test_get_cat(cat_id)
        test_create_sighting(cat_id)
        test_create_health_report(cat_id)
        test_create_feed_log(cat_id)
        
        print("\n" + "=" * 60)
        print("✅ 所有测试通过！")
        print("=" * 60)
        
    except Exception as e:
        print("\n" + "=" * 60)
        print(f"❌ 测试失败: {e}")
        print("=" * 60)
        import traceback
        traceback.print_exc()

