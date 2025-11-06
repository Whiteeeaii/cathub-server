"""
Cathub 后端服务器 - Flask REST API
支持猫咪档案、上报、投喂等功能
"""
from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import sqlite3
import os
import json
import time
import base64
from datetime import datetime
from werkzeug.utils import secure_filename
from PIL import Image
import io
import hashlib

# 导入 AI 识别模块
try:
    from ai_recognition import is_ai_available, recognize_cat_from_database, describe_cat_features
    AI_ENABLED = is_ai_available()
    print(f"🤖 AI 识别功能: {'已启用' if AI_ENABLED else '未启用（需要配置 GEMINI_API_KEY）'}")
except ImportError as e:
    AI_ENABLED = False
    print(f"⚠️ AI 识别模块导入失败: {str(e)}")

app = Flask(__name__)
CORS(app)  # 允许跨域访问

# 配置
# 使用绝对路径，确保在 Render 上也能正常工作
BASE_DIR = os.path.abspath(os.path.dirname(__file__))
UPLOAD_FOLDER = os.path.join(BASE_DIR, 'uploads')
DATABASE = os.path.join(BASE_DIR, 'cathub.db')
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}

# Flask 配置
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB 最大上传大小
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# 确保上传文件夹存在
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

print(f"📁 工作目录: {BASE_DIR}")
print(f"📁 数据库路径: {DATABASE}")
print(f"📁 上传文件夹: {UPLOAD_FOLDER}")

# ==================== 数据库初始化 ====================
def init_db():
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    # 猫咪档案表
    c.execute('''CREATE TABLE IF NOT EXISTS cats (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        sex TEXT,
        age_months INTEGER,
        pattern TEXT,
        activity_areas TEXT,
        personality TEXT,
        food_preferences TEXT,
        feeding_tips TEXT,
        photos TEXT,
        embeddings TEXT,
        created_by TEXT,
        created_at INTEGER,
        updated_at INTEGER
    )''')
    
    # 目击记录表
    c.execute('''CREATE TABLE IF NOT EXISTS sightings (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        cat_id INTEGER,
        photo TEXT,
        location TEXT,
        similarity REAL,
        device TEXT,
        reporter TEXT,
        ts INTEGER,
        FOREIGN KEY (cat_id) REFERENCES cats(id)
    )''')
    
    # 健康上报表
    c.execute('''CREATE TABLE IF NOT EXISTS health_reports (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        cat_id INTEGER,
        type TEXT,
        severity TEXT,
        note TEXT,
        photos TEXT,
        reporter TEXT,
        ts INTEGER,
        status TEXT,
        FOREIGN KEY (cat_id) REFERENCES cats(id)
    )''')
    
    # 投喂记录表
    c.execute('''CREATE TABLE IF NOT EXISTS feed_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        cat_id INTEGER,
        food TEXT,
        qty TEXT,
        note TEXT,
        reporter TEXT,
        ts INTEGER,
        FOREIGN KEY (cat_id) REFERENCES cats(id)
    )''')
    
    conn.commit()
    conn.close()
    print("✅ 数据库初始化完成")

# ==================== 工具函数 ====================
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def get_db():
    conn = sqlite3.connect(DATABASE)
    conn.row_factory = sqlite3.Row
    return conn

def save_photo(file):
    """保存上传的照片，返回文件路径"""
    if file and allowed_file(file.filename):
        filename = f"{int(time.time() * 1000)}_{secure_filename(file.filename)}"
        filepath = os.path.join(UPLOAD_FOLDER, filename)
        file.save(filepath)
        return filepath
    return None

def compute_image_hash(image_path):
    """计算图像的感知哈希值（用于相似度比较）"""
    try:
        print(f"  📷 处理图像: {image_path}")

        # 检查文件是否存在
        if not os.path.exists(image_path):
            print(f"  ❌ 文件不存在: {image_path}")
            return None

        # 打开图像
        img = Image.open(image_path)
        print(f"  ✅ 图像大小: {img.size}, 模式: {img.mode}")

        # 转换为 RGB（如果是 RGBA 或其他模式）
        if img.mode in ('RGBA', 'LA', 'P'):
            # 创建白色背景
            background = Image.new('RGB', img.size, (255, 255, 255))
            if img.mode == 'P':
                img = img.convert('RGBA')
            background.paste(img, mask=img.split()[-1] if img.mode in ('RGBA', 'LA') else None)
            img = background

        # 转换为灰度图
        img = img.convert('L')

        # 缩放到 8x8
        img = img.resize((8, 8), Image.Resampling.LANCZOS)

        # 计算平均值
        pixels = list(img.getdata())
        avg = sum(pixels) / len(pixels)

        # 生成哈希
        hash_str = ''.join(['1' if p > avg else '0' for p in pixels])
        print(f"  ✅ 哈希生成成功: {hash_str[:16]}...")

        return hash_str
    except Exception as e:
        print(f"  ❌ 计算图像哈希失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def hamming_distance(hash1, hash2):
    """计算两个哈希值的汉明距离"""
    if not hash1 or not hash2 or len(hash1) != len(hash2):
        return 100
    return sum(c1 != c2 for c1, c2 in zip(hash1, hash2))

def calculate_similarity(hash1, hash2):
    """计算相似度（0-100）"""
    distance = hamming_distance(hash1, hash2)
    similarity = (1 - distance / 64.0) * 100
    return max(0, similarity)

# ==================== 初始化数据库 ====================
# 在模块加载时初始化数据库（确保 gunicorn 启动时也会执行）
try:
    init_db()
except Exception as e:
    print(f"⚠️ 数据库初始化警告: {str(e)}")

# ==================== API 路由 ====================

@app.route('/api/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({"status": "ok", "message": "Cathub API is running"})

# ---------- 猫咪档案 API ----------
@app.route('/api/cats', methods=['GET'])
def get_cats():
    """获取所有猫咪列表"""
    try:
        print("📋 获取猫咪列表...")
        conn = get_db()
        cats = conn.execute('SELECT * FROM cats ORDER BY created_at DESC').fetchall()
        conn.close()

        result = []
        for cat in cats:
            result.append({
                'id': cat['id'],
                'name': cat['name'],
                'sex': cat['sex'],
                'age_months': cat['age_months'],
                'pattern': cat['pattern'],
                'activity_areas': json.loads(cat['activity_areas']) if cat['activity_areas'] else [],
                'personality': json.loads(cat['personality']) if cat['personality'] else [],
                'food_preferences': json.loads(cat['food_preferences']) if cat['food_preferences'] else [],
                'feeding_tips': cat['feeding_tips'],
                'photos': json.loads(cat['photos']) if cat['photos'] else [],
                'created_at': cat['created_at'],
                'updated_at': cat['updated_at']
            })

        print(f"✅ 返回 {len(result)} 只猫咪")
        return jsonify(result)
    except Exception as e:
        print(f"❌ 获取猫咪列表失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

@app.route('/api/cats/<int:cat_id>', methods=['GET'])
def get_cat(cat_id):
    """获取单个猫咪详情"""
    conn = get_db()
    cat = conn.execute('SELECT * FROM cats WHERE id = ?', (cat_id,)).fetchone()
    conn.close()
    
    if not cat:
        return jsonify({"error": "Cat not found"}), 404
    
    return jsonify({
        'id': cat['id'],
        'name': cat['name'],
        'sex': cat['sex'],
        'age_months': cat['age_months'],
        'pattern': cat['pattern'],
        'activity_areas': json.loads(cat['activity_areas']) if cat['activity_areas'] else [],
        'personality': json.loads(cat['personality']) if cat['personality'] else [],
        'food_preferences': json.loads(cat['food_preferences']) if cat['food_preferences'] else [],
        'feeding_tips': cat['feeding_tips'],
        'photos': json.loads(cat['photos']) if cat['photos'] else [],
        'embeddings': json.loads(cat['embeddings']) if cat['embeddings'] else [],
        'created_at': cat['created_at'],
        'updated_at': cat['updated_at']
    })

@app.route('/api/cats', methods=['POST'])
def create_cat():
    """创建猫咪档案"""
    try:
        print("📝 创建猫咪档案...")
        data = request.json
        print(f"收到数据: {data}")

        now = int(time.time())
        conn = get_db()
        cursor = conn.cursor()

        cursor.execute('''INSERT INTO cats
            (name, sex, age_months, pattern, activity_areas, personality,
             food_preferences, feeding_tips, photos, embeddings, created_by, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)''',
            (
                data.get('name'),
                data.get('sex'),
                data.get('age_months'),
                data.get('pattern'),
                json.dumps(data.get('activity_areas', []), ensure_ascii=False),
                json.dumps(data.get('personality', []), ensure_ascii=False),
                json.dumps(data.get('food_preferences', []), ensure_ascii=False),
                data.get('feeding_tips'),
                json.dumps(data.get('photos', []), ensure_ascii=False),
                json.dumps(data.get('embeddings', []), ensure_ascii=False),
                data.get('created_by', 'anonymous'),
                now,
                now
            ))
    
        cat_id = cursor.lastrowid
        conn.commit()
        conn.close()

        print(f"✅ 猫咪创建成功，ID: {cat_id}")
        return jsonify({"id": cat_id, "message": "Cat created successfully"}), 201
    except Exception as e:
        print(f"❌ 创建猫咪失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

@app.route('/api/cats/<int:cat_id>', methods=['PUT'])
def update_cat(cat_id):
    """更新猫咪档案"""
    data = request.json
    
    conn = get_db()
    cursor = conn.cursor()
    
    # 检查猫咪是否存在
    cat = cursor.execute('SELECT * FROM cats WHERE id = ?', (cat_id,)).fetchone()
    if not cat:
        conn.close()
        return jsonify({"error": "Cat not found"}), 404
    
    now = int(time.time())
    cursor.execute('''UPDATE cats SET 
        name = ?, sex = ?, age_months = ?, pattern = ?, 
        activity_areas = ?, personality = ?, food_preferences = ?, 
        feeding_tips = ?, photos = ?, embeddings = ?, updated_at = ?
        WHERE id = ?''',
        (
            data.get('name', cat['name']),
            data.get('sex', cat['sex']),
            data.get('age_months', cat['age_months']),
            data.get('pattern', cat['pattern']),
            json.dumps(data.get('activity_areas', []), ensure_ascii=False),
            json.dumps(data.get('personality', []), ensure_ascii=False),
            json.dumps(data.get('food_preferences', []), ensure_ascii=False),
            data.get('feeding_tips', cat['feeding_tips']),
            json.dumps(data.get('photos', []), ensure_ascii=False),
            json.dumps(data.get('embeddings', []), ensure_ascii=False),
            now,
            cat_id
        ))
    
    conn.commit()
    conn.close()
    
    return jsonify({"message": "Cat updated successfully"})

@app.route('/api/cats/<int:cat_id>/photos', methods=['POST'])
def upload_cat_photo(cat_id):
    """上传猫咪照片"""
    if 'photo' not in request.files:
        return jsonify({"error": "No photo provided"}), 400
    
    file = request.files['photo']
    filepath = save_photo(file)
    
    if not filepath:
        return jsonify({"error": "Invalid file type"}), 400
    
    # 更新猫咪的照片列表
    conn = get_db()
    cursor = conn.cursor()
    cat = cursor.execute('SELECT photos FROM cats WHERE id = ?', (cat_id,)).fetchone()
    
    if not cat:
        conn.close()
        return jsonify({"error": "Cat not found"}), 404
    
    photos = json.loads(cat['photos']) if cat['photos'] else []
    photos.append({
        "path": filepath,
        "uploaded_at": int(time.time())
    })
    
    cursor.execute('UPDATE cats SET photos = ?, updated_at = ? WHERE id = ?',
                   (json.dumps(photos, ensure_ascii=False), int(time.time()), cat_id))
    conn.commit()
    conn.close()
    
    return jsonify({"path": filepath, "message": "Photo uploaded successfully"})

@app.route('/api/recognize', methods=['POST'])
def recognize_cat():
    """识别猫咪 - 支持 AI 和传统方法"""
    temp_filepath = None
    try:
        # 检查是否使用 AI 识别
        use_ai = request.form.get('use_ai', 'false').lower() == 'true'

        print(f"🔍 开始识别猫咪... (方法: {'AI' if use_ai and AI_ENABLED else '传统哈希'})")

        if 'photo' not in request.files:
            print("❌ 没有收到照片文件")
            return jsonify({"error": "No photo provided"}), 400

        file = request.files['photo']
        print(f"📸 收到文件: {file.filename}, 大小: {file.content_length if hasattr(file, 'content_length') else 'unknown'}")

        # 保存临时文件
        temp_filepath = save_photo(file)
        if not temp_filepath:
            print("❌ 文件类型不支持")
            return jsonify({"error": "Invalid file type"}), 400

        print(f"✅ 临时文件已保存: {temp_filepath}")

        # 获取所有猫咪数据
        conn = get_db()
        cursor = conn.cursor()
        cats = cursor.execute('SELECT * FROM cats').fetchall()
        print(f"📊 找到 {len(cats)} 只猫咪")

        matches = []

        # 选择识别方法
        if use_ai and AI_ENABLED:
            # 使用 AI 识别
            print("🤖 使用 AI 识别...")
            cats_data = []
            for cat in cats:
                cats_data.append({
                    'id': cat['id'],
                    'name': cat['name'],
                    'sex': cat['sex'],
                    'age_months': cat['age_months'],
                    'pattern': cat['pattern'],
                    'activity_areas': json.loads(cat['activity_areas']) if cat['activity_areas'] else [],
                    'personality': json.loads(cat['personality']) if cat['personality'] else [],
                    'food_preferences': json.loads(cat['food_preferences']) if cat['food_preferences'] else [],
                    'feeding_tips': cat['feeding_tips'],
                    'photos': json.loads(cat['photos']) if cat['photos'] else [],
                    'embeddings': json.loads(cat['embeddings']) if cat['embeddings'] else [],
                    'created_at': cat['created_at'],
                    'updated_at': cat['updated_at']
                })

            ai_matches = recognize_cat_from_database(temp_filepath, cats_data)

            for match in ai_matches:
                cat_data = match['cat']
                cat_data['similarity'] = match['similarity']
                matches.append(cat_data)

        else:
            # 使用传统哈希方法
            print("🔢 使用传统哈希识别...")
            upload_hash = compute_image_hash(temp_filepath)
            if not upload_hash:
                print("❌ 图像处理失败")
                return jsonify({"error": "Failed to process image"}), 500

            print(f"✅ 图像哈希: {upload_hash[:16]}...")

            for cat in cats:
                photos = json.loads(cat['photos']) if cat['photos'] else []

                if not photos:
                    continue

                # 计算与每张照片的相似度
                max_similarity = 0
                for photo in photos:
                    photo_path = photo.get('path')
                    if photo_path and os.path.exists(photo_path):
                        photo_hash = compute_image_hash(photo_path)
                        if photo_hash:
                            similarity = calculate_similarity(upload_hash, photo_hash)
                            max_similarity = max(max_similarity, similarity)

                # 如果相似度超过阈值，添加到匹配列表
                if max_similarity > 30:  # 30% 相似度阈值
                    print(f"✅ 匹配: {cat['name']} (相似度: {max_similarity:.2f}%)")
                    matches.append({
                        'id': cat['id'],
                        'name': cat['name'],
                        'sex': cat['sex'],
                        'age_months': cat['age_months'],
                        'pattern': cat['pattern'],
                        'activity_areas': json.loads(cat['activity_areas']) if cat['activity_areas'] else [],
                        'personality': json.loads(cat['personality']) if cat['personality'] else [],
                        'food_preferences': json.loads(cat['food_preferences']) if cat['food_preferences'] else [],
                        'feeding_tips': cat['feeding_tips'],
                        'photos': json.loads(cat['photos']) if cat['photos'] else [],
                        'embeddings': json.loads(cat['embeddings']) if cat['embeddings'] else [],
                        'created_at': cat['created_at'],
                        'updated_at': cat['updated_at'],
                        'similarity': round(max_similarity, 2)
                    })

        conn.close()

        # 按相似度排序
        matches.sort(key=lambda x: x['similarity'], reverse=True)

        print(f"🎯 识别完成，找到 {len(matches)} 个匹配")

        # 删除临时文件
        if temp_filepath:
            try:
                os.remove(temp_filepath)
                print(f"🗑️ 临时文件已删除")
            except Exception as e:
                print(f"⚠️ 删除临时文件失败: {str(e)}")

        return jsonify({
            "matches": matches,
            "count": len(matches)
        })

    except Exception as e:
        print(f"❌ 识别失败: {str(e)}")
        import traceback
        traceback.print_exc()

        # 清理临时文件
        if temp_filepath:
            try:
                os.remove(temp_filepath)
            except:
                pass

        return jsonify({"error": str(e)}), 500

# ---------- 目击记录 API ----------
@app.route('/api/sightings', methods=['POST'])
def create_sighting():
    """创建目击记录"""
    data = request.json
    
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('''INSERT INTO sightings 
        (cat_id, photo, location, similarity, device, reporter, ts)
        VALUES (?, ?, ?, ?, ?, ?, ?)''',
        (
            data.get('cat_id'),
            data.get('photo'),
            data.get('location'),
            data.get('similarity'),
            data.get('device'),
            data.get('reporter', 'anonymous'),
            int(time.time())
        ))
    
    sighting_id = cursor.lastrowid
    conn.commit()
    conn.close()
    
    return jsonify({"id": sighting_id, "message": "Sighting created successfully"}), 201

@app.route('/api/sightings', methods=['GET'])
def get_sightings():
    """获取目击记录"""
    cat_id = request.args.get('cat_id')
    
    conn = get_db()
    if cat_id:
        sightings = conn.execute('SELECT * FROM sightings WHERE cat_id = ? ORDER BY ts DESC', (cat_id,)).fetchall()
    else:
        sightings = conn.execute('SELECT * FROM sightings ORDER BY ts DESC LIMIT 100').fetchall()
    conn.close()
    
    result = [dict(s) for s in sightings]
    return jsonify(result)

# ---------- 健康上报 API ----------
@app.route('/api/health_reports', methods=['POST'])
def create_health_report():
    """创建健康上报"""
    data = request.json
    
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('''INSERT INTO health_reports 
        (cat_id, type, severity, note, photos, reporter, ts, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)''',
        (
            data.get('cat_id'),
            data.get('type'),
            data.get('severity'),
            data.get('note'),
            json.dumps(data.get('photos', []), ensure_ascii=False),
            data.get('reporter', 'anonymous'),
            int(time.time()),
            data.get('status', 'pending')
        ))
    
    report_id = cursor.lastrowid
    conn.commit()
    conn.close()
    
    return jsonify({"id": report_id, "message": "Health report created successfully"}), 201

@app.route('/api/health_reports', methods=['GET'])
def get_health_reports():
    """获取健康上报"""
    cat_id = request.args.get('cat_id')
    
    conn = get_db()
    if cat_id:
        reports = conn.execute('SELECT * FROM health_reports WHERE cat_id = ? ORDER BY ts DESC', (cat_id,)).fetchall()
    else:
        reports = conn.execute('SELECT * FROM health_reports ORDER BY ts DESC LIMIT 100').fetchall()
    conn.close()
    
    result = []
    for r in reports:
        result.append({
            'id': r['id'],
            'cat_id': r['cat_id'],
            'type': r['type'],
            'severity': r['severity'],
            'note': r['note'],
            'photos': json.loads(r['photos']) if r['photos'] else [],
            'reporter': r['reporter'],
            'ts': r['ts'],
            'status': r['status']
        })
    
    return jsonify(result)

# ---------- 投喂记录 API ----------
@app.route('/api/feed_logs', methods=['POST'])
def create_feed_log():
    """创建投喂记录"""
    data = request.json
    
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('''INSERT INTO feed_logs 
        (cat_id, food, qty, note, reporter, ts)
        VALUES (?, ?, ?, ?, ?, ?)''',
        (
            data.get('cat_id'),
            data.get('food'),
            data.get('qty'),
            data.get('note'),
            data.get('reporter', 'anonymous'),
            int(time.time())
        ))
    
    log_id = cursor.lastrowid
    conn.commit()
    conn.close()
    
    return jsonify({"id": log_id, "message": "Feed log created successfully"}), 201

@app.route('/api/feed_logs', methods=['GET'])
def get_feed_logs():
    """获取投喂记录"""
    cat_id = request.args.get('cat_id')
    
    conn = get_db()
    if cat_id:
        logs = conn.execute('SELECT * FROM feed_logs WHERE cat_id = ? ORDER BY ts DESC', (cat_id,)).fetchall()
    else:
        logs = conn.execute('SELECT * FROM feed_logs ORDER BY ts DESC LIMIT 100').fetchall()
    conn.close()
    
    result = [dict(log) for log in logs]
    return jsonify(result)

# ---------- 照片访问 ----------
@app.route('/uploads/<path:filename>')
def uploaded_file(filename):
    """访问上传的照片"""
    return send_from_directory(UPLOAD_FOLDER, filename)

# ==================== 启动服务器 ====================
if __name__ == '__main__':
    init_db()
    port = int(os.environ.get('PORT', 5000))
    print("=" * 50)
    print("🐱 Cathub 后端服务器启动中...")
    print("=" * 50)
    print("📡 API 地址: http://localhost:{port}")
    print("📝 API 文档:")
    print("   GET  /api/health - 健康检查")
    print("   GET  /api/cats - 获取所有猫咪")
    print("   POST /api/cats - 创建猫咪档案")
    print("   GET  /api/cats/<id> - 获取猫咪详情")
    print("   PUT  /api/cats/<id> - 更新猫咪档案")
    print("   POST /api/cats/<id>/photos - 上传照片")
    print("   POST /api/sightings - 创建目击记录")
    print("   POST /api/health_reports - 创建健康上报")
    print("   POST /api/feed_logs - 创建投喂记录")
    print("=" * 50)
    app.run(host='0.0.0.0', port=port, debug=False)

