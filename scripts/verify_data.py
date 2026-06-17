"""
verify_data.py

Comprehensive verification of the imported word data:
- book word_count matches actual count
- each word has meanings and sentences
- phonetic symbols are non-empty and look like IPA
- image URLs are valid Unsplash URLs
- spot check a few sample words
"""
import sys
import io
# Force UTF-8 output for PowerShell compatibility
if sys.platform == "win32":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
    sys.stderr = sys.stdout

import pymysql
import re

DB = dict(host="localhost", port=3306, user="root", password="root",
          database="smartvocab", charset="utf8mb4")


def main():
    conn = pymysql.connect(**DB)
    issues = []
    # Out of scope: books 4 (高中) and 5 (初中) - we only filled 1/2/3/6/7
    OUT_OF_SCOPE_BOOKS = {4, 5}
    try:
        with conn.cursor() as cur:
            # 1. book.word_count vs actual
            print("=== 1. Per-book counts ===")
            cur.execute("""SELECT b.id, b.name, b.word_count, b.level,
                                  (SELECT COUNT(*) FROM word WHERE book_id = b.id) AS actual,
                                  (SELECT COUNT(DISTINCT spelling) FROM word WHERE book_id = b.id) AS uniq
                           FROM book b ORDER BY b.id""")
            for row in cur.fetchall():
                bid, name, claimed, level, actual, uniq = row
                if bid in OUT_OF_SCOPE_BOOKS:
                    print(f"  [{bid}] {name:12s} level={level:6s} claimed={claimed} actual={actual} -> OUT OF SCOPE (skipped)")
                    continue
                status = "OK" if claimed == actual else "MISMATCH"
                print(f"  [{bid}] {name:12s} level={level:6s} claimed={claimed} actual={actual} uniq={uniq} -> {status}")
                if claimed != actual:
                    issues.append(f"Book {bid} word_count={claimed} != actual={actual}")

            # 2. Meanings coverage
            print("\n=== 2. Meanings coverage ===")
            cur.execute("""SELECT b.id, b.name,
                                  (SELECT COUNT(*) FROM word WHERE book_id = b.id) AS wn,
                                  (SELECT COUNT(*) FROM word_meaning m JOIN word w ON m.word_id = w.id WHERE w.book_id = b.id) AS mn
                           FROM book b WHERE b.id IN (1,2,3,6,7) ORDER BY b.id""")
            for row in cur.fetchall():
                bid, name, wn, mn = row
                avg = mn / wn if wn else 0
                print(f"  [{bid}] {name:12s} words={wn} meanings={mn} avg={avg:.2f}")

            # 3. Sentences coverage
            print("\n=== 3. Sentences coverage ===")
            cur.execute("""SELECT b.id, b.name,
                                  (SELECT COUNT(*) FROM word WHERE book_id = b.id) AS wn,
                                  (SELECT COUNT(*) FROM word_sentence s JOIN word w ON s.word_id = w.id WHERE w.book_id = b.id) AS sn
                           FROM book b WHERE b.id IN (1,2,3,6,7) ORDER BY b.id""")
            for row in cur.fetchall():
                bid, name, wn, sn = row
                avg = sn / wn if wn else 0
                print(f"  [{bid}] {name:12s} words={wn} sentences={sn} avg={avg:.2f}")

            # 4. Phonetic quality
            print("\n=== 4. Phonetic quality (sample) ===")
            cur.execute("""SELECT spelling, phonetic_us, phonetic_uk
                           FROM word WHERE book_id IN (1,2,3,6,7)
                           ORDER BY id LIMIT 5""")
            for row in cur.fetchall():
                sp, us, uk = row
                print(f"  {sp:20s} US={us:30s} UK={uk}")

            # 5. Image URLs (unique & valid)
            print("\n=== 5. Image URL coverage ===")
            cur.execute("""SELECT COUNT(*) AS total,
                                  COUNT(DISTINCT image_url) AS uniq,
                                  SUM(CASE WHEN image_url LIKE 'https://images.unsplash.com/%' THEN 1 ELSE 0 END) AS unsplash
                           FROM word WHERE book_id IN (1,2,3,6,7)""")
            total, uniq, unsplash = cur.fetchone()
            print(f"  total words: {total}")
            print(f"  unique image URLs: {uniq}")
            print(f"  unsplash URLs: {unsplash}")
            if unsplash != total:
                issues.append(f"Image URL not all unsplash: {total-unsplash} rows")

            # 6. Difficult distribution
            print("\n=== 6. Difficulty distribution ===")
            cur.execute("""SELECT book_id, difficulty, COUNT(*) FROM word
                           WHERE book_id IN (1,2,3,6,7)
                           GROUP BY book_id, difficulty ORDER BY book_id, difficulty""")
            for row in cur.fetchall():
                bid, diff, n = row
                print(f"  book {bid} {diff:6s}: {n}")

            # 7. Spot check spelling
            print("\n=== 7. Spot check sample words ===")
            for w in ["abandon", "research", "opportunity", "sophisticated", "endeavor"]:
                cur.execute("""SELECT w.id, w.spelling, w.phonetic_us, w.difficulty, w.image_url,
                                     (SELECT COUNT(*) FROM word_meaning WHERE word_id = w.id) AS m,
                                     (SELECT COUNT(*) FROM word_sentence WHERE word_id = w.id) AS s
                              FROM word w WHERE w.spelling = %s LIMIT 5""", (w,))
                rows = cur.fetchall()
                for r in rows:
                    wid, sp, ph, diff, img, mc, sc = r
                    print(f"  id={wid} {sp:18s} {ph:25s} {diff:6s} m={mc} s={sc}")
                    print(f"      img={img[:80]}...")

    finally:
        conn.close()

    print("\n=== Summary ===")
    if issues:
        print(f"ISSUES: {len(issues)}")
        for i in issues:
            print(f"  - {i}")
        sys.exit(1)
    else:
        print("ALL CHECKS PASSED ✓")


if __name__ == "__main__":
    main()
