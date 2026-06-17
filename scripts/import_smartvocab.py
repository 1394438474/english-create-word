"""
import_smartvocab.py

Imports db/smartvocab.sql into the local MySQL server using pymysql.
- Reads file as UTF-8 (BOM tolerated)
- Splits on semicolons (the file is simple INSERT/DELETE/CREATE statements)
- Runs statement by statement, reporting progress and errors
- Verifies the row counts in the main tables at the end
"""
import os
import re
import sys
import time
import pymysql

PROJECT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SQL_FILE = os.path.join(PROJECT_DIR, "db", "smartvocab.sql")

# MySQL connection (matches application.yml)
DB_CONFIG = dict(
    host="localhost",
    port=3306,
    user="root",
    password="root",
    database="smartvocab",
    charset="utf8mb4",
    autocommit=False,
    local_infile=False,
)


def split_statements(sql: str) -> list:
    """Split SQL into individual statements, ignoring comments and empty ones."""
    # Strip line comments (-- ...)
    out = []
    buf = []
    for raw_line in sql.splitlines():
        line = raw_line
        # remove trailing line comments
        if "--" in line:
            idx = line.find("--")
            # only treat as comment if not inside a string - we use a simple
            # heuristic (comment at start of line OR after whitespace)
            if idx == 0 or line[:idx].strip() == "":
                line = line[:idx]
        if not line.strip():
            continue
        buf.append(line)
        # naive: split on ; at end of line
        if line.rstrip().endswith(";"):
            stmt = "\n".join(buf).strip().rstrip(";").strip()
            if stmt:
                out.append(stmt)
            buf = []
    if buf:
        stmt = "\n".join(buf).strip()
        if stmt:
            out.append(stmt)
    return out


def main():
    if not os.path.exists(SQL_FILE):
        print(f"ERROR: {SQL_FILE} not found")
        sys.exit(1)
    print(f"Reading {SQL_FILE} ...")
    with open(SQL_FILE, "rb") as f:
        raw = f.read()
    # Strip BOM
    if raw.startswith(b"\xef\xbb\xbf"):
        raw = raw[3:]
    sql = raw.decode("utf-8")
    print(f"  size: {len(raw) / 1024 / 1024:.2f} MB")

    print("Splitting statements ...")
    stmts = split_statements(sql)
    print(f"  total statements: {len(stmts)}")
    # quick bucket
    buckets = {}
    for s in stmts:
        head = s.lstrip().split(None, 1)[0].upper() if s.strip() else "(empty)"
        buckets[head] = buckets.get(head, 0) + 1
    for k, v in sorted(buckets.items(), key=lambda x: -x[1]):
        print(f"    {k}: {v}")

    print("\nConnecting to MySQL ...")
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            ok = 0
            err = 0
            t0 = time.time()
            for i, stmt in enumerate(stmts, 1):
                head = stmt.lstrip().split(None, 1)[0].upper()
                try:
                    cur.execute(stmt)
                    ok += 1
                except Exception as e:
                    err += 1
                    snippet = stmt[:120].replace("\n", " ")
                    print(f"  [{i}] ERR ({head}): {e}")
                    print(f"      SQL: {snippet}...")
                # commit in batches
                if i % 50 == 0:
                    conn.commit()
                    elapsed = time.time() - t0
                    print(f"  [{i}/{len(stmts)}] committed (ok={ok}, err={err}, {elapsed:.1f}s)")
            conn.commit()
            elapsed = time.time() - t0
            print(f"\nDone. ok={ok} err={err} in {elapsed:.1f}s")

            # verify counts
            print("\n=== Verification ===")
            tables = ["book", "word", "word_meaning", "word_sentence", "user", "medal"]
            for t in tables:
                cur.execute(f"SELECT COUNT(*) FROM `{t}`")
                (n,) = cur.fetchone()
                print(f"  {t:15s}: {n}")
            # per-book word count
            print("\nPer-book word counts:")
            cur.execute("SELECT id, name, word_count, (SELECT COUNT(*) FROM word WHERE book_id = b.id) AS actual FROM book b ORDER BY id")
            for row in cur.fetchall():
                bid, name, claimed, actual = row
                print(f"  [{bid}] {name:12s} claimed={claimed} actual={actual}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
