"""并发版：解析 loremflickr URL → 多次重试，必要时用 GET 替代 HEAD."""
import subprocess
import urllib.request
import urllib.error
import concurrent.futures
import time
import os

MYSQL = r'C:\dev-tools\mysql-8.0.39-winx64\bin\mysql.exe'
DB = 'smartvocab'

def mysql(sql):
    r = subprocess.run(
        [MYSQL, '-uroot', '-proot', DB, '--default-character-set=utf8mb4', '-N', '-e', sql],
        capture_output=True, text=True, encoding='utf-8', timeout=60
    )
    return r.stdout if r.returncode == 0 else ''

def resolve_one(url, timeout=12):
    """HEAD then GET fallback. 多次重试."""
    for i in range(3):
        for method in ('HEAD', 'GET'):
            try:
                req = urllib.request.Request(url, method=method)
                if method == 'GET':
                    # GET 时不开大 body
                    resp = urllib.request.urlopen(req, timeout=timeout)
                    resp.read(1)  # 触发连接
                else:
                    resp = urllib.request.urlopen(req, timeout=timeout)
                # resp.url 是最终跳转地址
                if resp.url != url and 'cache' in resp.url:
                    return resp.url
                # 如果还是 proxy，可能是 loremflickr 暂时没缓存；等一下再试
                time.sleep(0.3)
            except Exception:
                time.sleep(0.3)
    return url

# 1) 列出所有 unique URL
print("Loading distinct URLs...", flush=True)
out = mysql("SELECT image_url FROM word GROUP BY image_url")
urls = [l.strip() for l in out.splitlines() if l.strip()]
print(f"Total distinct: {len(urls)}", flush=True)

# 2) 并发解析（最多 16 并发）
print("Resolving in parallel (16 workers)...", flush=True)
resolved = {}
start = time.time()
done = 0
with concurrent.futures.ThreadPoolExecutor(max_workers=16) as ex:
    futures = {ex.submit(resolve_one, u): u for u in urls}
    for fut in concurrent.futures.as_completed(futures):
        u = futures[fut]
        resolved[u] = fut.result()
        done += 1
        if done % 200 == 0 or done == len(urls):
            print(f"  {done}/{len(urls)}  elapsed={time.time()-start:.1f}s", flush=True)
print(f"Done in {time.time()-start:.1f}s", flush=True)

# 3) 生成 SQL
print("Generating SQL...", flush=True)
sql_path = r'C:\Users\xl\Desktop\英语单词\scripts\resolve_images.sql'
with open(sql_path, 'w', encoding='utf-8') as f:
    f.write('SET NAMES utf8mb4;\n')
    upd = 0
    for orig, final in resolved.items():
        if final != orig:
            f.write(f"UPDATE word SET image_url = '{final.replace(chr(39), chr(39)+chr(39))}' "
                    f"WHERE image_url = '{orig.replace(chr(39), chr(39)+chr(39))}';\n")
            upd += 1
    print(f"Updates to apply: {upd}", flush=True)
print(f"SQL size: {os.path.getsize(sql_path)} bytes", flush=True)

# 4) 导入
print("Importing...", flush=True)
with open(sql_path, 'r', encoding='utf-8') as f:
    sql_text = f.read()
r = subprocess.run(
    [MYSQL, '-uroot', '-proot', DB, '--default-character-set=utf8mb4'],
    input=sql_text, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=120
)
if r.returncode != 0:
    print(f"Import ERR: {r.stderr[:500]}", flush=True)
else:
    print("Import OK", flush=True)

# 5) 验证
print("\n=== Final Sample ===", flush=True)
out = mysql("""SELECT w.id, w.spelling, w.image_url
               FROM word w WHERE w.book_id=1 ORDER BY w.id LIMIT 5""")
for line in out.splitlines():
    parts = line.split('\t')
    if len(parts) == 3:
        print(f"  {parts[0]:5s}  {parts[1]:15s}  {parts[2]}", flush=True)
