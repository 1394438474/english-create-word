"""
为 word.image_url 重生为基于英文单词的 loremflickr URL。
URL pattern: https://loremflickr.com/800/600/{keyword}
loremflickr 是按关键词从 Flickr 搜索的图片代理，每次返回与关键词语义匹配的真实图片（带缓存）。
"""
import subprocess
import os
import urllib.parse
import re
import sys

MYSQL = r'C:\dev-tools\mysql-8.0.39-winx64\bin\mysql.exe'
DB = 'smartvocab'


def mysql(sql: str) -> str:
    r = subprocess.run(
        [MYSQL, '-uroot', '-proot', DB, '--default-character-set=utf8mb4', '-N', '-e', sql],
        capture_output=True, text=True, encoding='utf-8'
    )
    if r.returncode != 0:
        print(f"SQL ERR: {r.stderr[:200]}", flush=True)
    return r.stdout


def to_keyword(spelling: str) -> str:
    """英文单词 → loremflickr 关键词。"""
    s = spelling.strip().lower()
    # 括号内容去掉
    s = re.sub(r'\([^)]*\)', '', s)
    s = s.strip()
    if not s:
        return 'word'
    # 多词短语：取第一个
    if ' ' in s:
        s = s.split(' ')[0]
    # 移除多余特殊字符
    s = re.sub(r"[^a-z0-9'-]", '', s)
    s = s.strip('-').strip("'")
    if not s:
        return 'word'
    return s


def build_url(spelling: str) -> str:
    kw = to_keyword(spelling)
    return f"https://loremflickr.com/800/600/{urllib.parse.quote(kw, safe='')}?lock=42"


# 1) 取所有 (id, spelling)
print("Loading words...", flush=True)
rows = []
out = mysql("SELECT id, spelling FROM word ORDER BY id")
for line in out.splitlines():
    parts = line.split('\t')
    if len(parts) == 2:
        rows.append((int(parts[0]), parts[1]))
print(f"Total: {len(rows)}", flush=True)

# 2) 生成 URL 集合（去重）
url_set = {}
url_by_id = {}
for wid, sp in rows:
    url = build_url(sp)
    url_by_id[wid] = url
    url_set[url] = url_set.get(url, 0) + 1
print(f"Unique URLs: {len(url_set)}", flush=True)

# 3) 生成 SQL（按 1000/批）
print("Generating SQL...", flush=True)
sql_path = r'C:\Users\xl\Desktop\英语单词\scripts\refresh_images.sql'
with open(sql_path, 'w', encoding='utf-8') as f:
    f.write('SET NAMES utf8mb4;\n')
    for i in range(0, len(rows), 1000):
        chunk = rows[i:i+1000]
        f.write("UPDATE word SET image_url = CASE id\n")
        for wid, sp in chunk:
            url = url_by_id[wid].replace("'", "''")
            f.write(f"  WHEN {wid} THEN '{url}'\n")
        ids = ','.join(str(w) for w, _ in chunk)
        f.write(f"END WHERE id IN ({ids});\n")
print(f"SQL size: {os.path.getsize(sql_path)} bytes", flush=True)

# 4) 导入
print("Importing...", flush=True)
with open(sql_path, 'r', encoding='utf-8') as f:
    sql_text = f.read()
r = subprocess.run(
    [MYSQL, '-uroot', '-proot', DB, '--default-character-set=utf8mb4'],
    input=sql_text, capture_output=True, text=True, encoding='utf-8', errors='replace'
)
if r.returncode != 0:
    print(f"Import ERR: {r.stderr[:500]}", flush=True)
else:
    print("Import OK", flush=True)

# 5) 验证
print("\n=== Sample ===", flush=True)
out = mysql("""SELECT w.id, w.spelling, m.meaning_zh, w.image_url
               FROM word w
               JOIN word_meaning m ON m.word_id=w.id
               WHERE m.sort=1 AND w.book_id=1
               ORDER BY w.id LIMIT 10""")
for line in out.splitlines():
    parts = line.split('\t')
    if len(parts) == 4:
        print(f"  {parts[0]:5s}  {parts[1]:15s}  {parts[2][:30]:30s}  {parts[3]}", flush=True)
