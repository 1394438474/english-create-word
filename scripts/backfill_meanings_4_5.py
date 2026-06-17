#!/usr/bin/env python3
"""
为 book 4/5 的词补全释义和例句。
策略：基于 spelling 匹配已有词的 meaning/sentence。
"""
import subprocess, os

MYSQL = r'C:\dev-tools\mysql-8.0.39-winx64\bin\mysql.exe'

def mysql(sql):
    r = subprocess.run([MYSQL, '-uroot', '-proot', 'smartvocab',
                        '--default-character-set=utf8mb4', '-N', '-e', sql],
                       capture_output=True, text=True, encoding='utf-8')
    if r.returncode != 0: print(f"ERR: {r.stderr[:200]}", flush=True)
    return r.stdout

# 1. 找 book 4/5 中没有 meaning 的词
need = []
for line in mysql("""SELECT w.id, w.spelling
                     FROM word w
                     LEFT JOIN word_meaning m ON m.word_id = w.id
                     WHERE w.book_id IN (4, 5)
                     GROUP BY w.id, w.spelling
                     HAVING COUNT(m.id) = 0""").splitlines():
    parts = line.split('\t')
    if len(parts) == 2:
        need.append((int(parts[0]), parts[1]))
print(f"无释义: {len(need)}", flush=True)

# 2. 取 source spelling → (pos, zh, en)
means_map = {}
for line in mysql("""SELECT w.spelling, m.pos, m.meaning_zh, m.meaning_en
                     FROM word w
                     JOIN word_meaning m ON m.word_id = w.id
                     WHERE w.id = (SELECT MIN(id) FROM word
                                   WHERE spelling = w.spelling AND book_id IN (1,2,3,6,7))
                     ORDER BY w.spelling, m.sort""").splitlines():
    parts = line.split('\t')
    if len(parts) == 4:
        sp, pos, dzh, den = parts
        means_map.setdefault(sp, []).append((pos, dzh, den))

# 3. 取 source spelling → (en, zh)
sents_map = {}
for line in mysql("""SELECT w.spelling, s.en, s.zh
                     FROM word w
                     JOIN word_sentence s ON s.word_id = w.id
                     WHERE w.id = (SELECT MIN(id) FROM word
                                   WHERE spelling = w.spelling AND book_id IN (1,2,3,6,7))
                     ORDER BY w.spelling, s.id""").splitlines():
    parts = line.split('\t')
    if len(parts) == 3:
        sp, sen, szh = parts
        sents_map.setdefault(sp, []).append((sen, szh))

print(f"source: {len(means_map)} meanings, {len(sents_map)} sentences", flush=True)

# 4. 生成
meaning_rows = []
sentence_rows = []
matched_m = 0
matched_s = 0
for wid, sp in need:
    ms = means_map.get(sp, [])
    ss = sents_map.get(sp, [])
    if ms: matched_m += 1
    if ss: matched_s += 1
    for j, (pos, dzh, den) in enumerate(ms[:2]):
        meaning_rows.append(f"({wid}, {j+1}, '{pos.replace(chr(39), chr(39)+chr(39))}', '{dzh.replace(chr(39), chr(39)+chr(39))}', '{den.replace(chr(39), chr(39)+chr(39))}')")
    for j, (sen, szh) in enumerate(ss[:1]):
        sentence_rows.append(f"({wid}, '{sen.replace(chr(39), chr(39)+chr(39))}', '{szh.replace(chr(39), chr(39)+chr(39))}', NULL)")
print(f"  matched: {matched_m} meanings, {matched_s} sentences", flush=True)
print(f"  生成: {len(meaning_rows)} meaning rows, {len(sentence_rows)} sentence rows", flush=True)

# 5. 写文件（防命令行超长）
sql_path = r'C:\Users\xl\Desktop\英语单词\scripts\backfill_4_5.sql'
with open(sql_path, 'w', encoding='utf-8') as f:
    f.write('SET NAMES utf8mb4;\n')
    for s in range(0, len(meaning_rows), 500):
        chunk = meaning_rows[s:s+500]
        f.write("INSERT INTO word_meaning (word_id, sort, pos, meaning_zh, meaning_en) VALUES\n")
        f.write(",\n".join(chunk) + ";\n")
    for s in range(0, len(sentence_rows), 500):
        chunk = sentence_rows[s:s+500]
        f.write("INSERT INTO word_sentence (word_id, en, zh, source) VALUES\n")
        f.write(",\n".join(chunk) + ";\n")
print(f"  写入 {sql_path} ({os.path.getsize(sql_path)} bytes)", flush=True)

# 6. 通过 stdin 导入
with open(sql_path, 'r', encoding='utf-8') as f:
    sql_text = f.read()
r = subprocess.run([MYSQL, '-uroot', '-proot', 'smartvocab', '--default-character-set=utf8mb4'],
                   input=sql_text,
                   capture_output=True, text=True, encoding='utf-8', errors='replace')
if r.returncode != 0:
    print(f"  import ERR: {r.stderr[:1000]}", flush=True)
else:
    print(f"  import OK", flush=True)

# 7. 验证
print("\n=== 验证 ===", flush=True)
print(mysql("""SELECT w.book_id, COUNT(DISTINCT w.id) AS words,
                      COUNT(DISTINCT m.word_id) AS with_meaning,
                      COUNT(DISTINCT s.word_id) AS with_sent
               FROM word w
               LEFT JOIN word_meaning m ON m.word_id = w.id
               LEFT JOIN word_sentence s ON s.word_id = w.id
               GROUP BY w.book_id ORDER BY w.book_id"""), flush=True)
