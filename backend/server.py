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

app = Flask(__name__)
CORS(app)  # 允许跨域访问

# 配置
# 使用绝对路径，确保在 Render 上也能正常工作
BASE_DIR = os.path.abspath(os.path.dirname(__file__))
UPLOAD_FOLDER = os.path.join(BASE_DIR, 'uploads')
DATABASE = os.path.join(BASE_DIR, 'cathub.db')
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}

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

