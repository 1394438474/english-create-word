"""
merge_to_smartvocab.py

Merge the auto-generated all_books.sql into the main db/smartvocab.sql
by inserting the DELETE/UPDATE/INSERT block right after the seed
word_sentence section (id 1-25), preserving the rest of the file
(medal/user/check-in/quiz etc.) untouched.

Also re-saves the resulting file with UTF-8 BOM so PowerShell / MySQL CLI
imports Chinese characters correctly.
"""
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.dirname(SCRIPT_DIR)
MAIN_SQL = os.path.join(PROJECT_DIR, "db", "smartvocab.sql")
GEN_SQL = os.path.join(SCRIPT_DIR, "all_books.sql")
OUT_SQL = MAIN_SQL  # overwrite in place

# Anchor markers in the main SQL where we want to insert
INSERT_BEFORE_MARKER = "-- 勋章定义"
GEN_HEADER_PREFIX = "-- ====="

def main():
    # Read raw bytes so we can strip existing BOM
    with open(MAIN_SQL, "rb") as f:
        raw = f.read()
    # Strip any leading BOM(s)
    while raw.startswith(b"\xef\xbb\xbf"):
        raw = raw[3:]
    main_text = raw.decode("utf-8")

    with open(GEN_SQL, "r", encoding="utf-8") as f:
        gen_text = f.read()

    # Strip the auto-generated comment header from gen_text (we'll add our own)
    lines = gen_text.splitlines()
    # Skip leading comment lines starting with --
    body_start = 0
    for i, ln in enumerate(lines):
        s = ln.strip()
        if s.startswith("--"):
            body_start = i + 1
            continue
        if not s:
            continue
        body_start = i
        break
    gen_body = "\n".join(lines[body_start:]).lstrip("\n")

    # If a previous auto-generated block exists, remove it first
    # (idempotency: look for our header marker)
    AUTO_HEADER = "-- 词书全量数据"
    if AUTO_HEADER in main_text:
        idx = main_text.find(AUTO_HEADER)
        # Walk back to find the start of the comment block (===...)
        # The block is delimited by a header like:
        #   -- ============================================================
        #   -- 词书全量数据 ...
        # ... ...
        # -- ============================================================
        # Walk back to the previous "============" line
        scan = idx
        block_start = idx
        while scan > 0:
            # find previous newline
            prev_nl = main_text.rfind("\n", 0, scan)
            line = main_text[prev_nl + 1: scan].strip()
            if line.startswith("--") and "=" in line:
                block_start = prev_nl + 1
                break
            if line == "":
                # blank line before our block - keep walking back
                scan = prev_nl
                continue
            break

        # Walk forward to find the end of the comment block
        # find the end line "============" right after the header
        scan = idx
        # skip to the first line that does NOT start with "--"
        while scan < len(main_text):
            nl = main_text.find("\n", scan)
            if nl == -1:
                break
            line = main_text[scan:nl].strip()
            scan = nl + 1
            if line.startswith("--") and "=" in line:
                continue
            if line.startswith("--") or line == "":
                continue
            # we hit the first non-comment, non-blank line - block_start..scan is the comment header
            block_end = scan
            break
        else:
            block_end = len(main_text)

        # Also consume trailing blank lines after the auto-generated content
        # up to INSERT_BEFORE_MARKER
        after_end = main_text.find(INSERT_BEFORE_MARKER, block_end)
        if after_end != -1:
            # consume blank lines
            scan2 = after_end
            while scan2 > block_end:
                prev_nl = main_text.rfind("\n", 0, scan2 - 1)
                line = main_text[prev_nl + 1: scan2].strip()
                if line == "":
                    scan2 = prev_nl + 1
                else:
                    break
            block_end = scan2

        print(f"  removing existing auto-generated block: offset {block_start}..{block_end}")
        main_text = main_text[:block_start] + main_text[block_end:]

    if INSERT_BEFORE_MARKER not in main_text:
        print(f"ERROR: anchor '{INSERT_BEFORE_MARKER}' not found in {MAIN_SQL}")
        sys.exit(1)

    # The new content block
    block = (
        "\n\n-- ============================================================\n"
        "-- 词书全量数据 (由 gen_all_books.py 自动生成)\n"
        f"-- 源文件: {os.path.basename(GEN_SQL)}\n"
        "-- 覆盖范围: book_id 1 (CET4), 2 (CET6), 3 (KAOYAN), 6 (IELTS), 7 (ZHUANSHENG)\n"
        "-- 执行顺序: DELETE -> UPDATE book.word_count -> INSERT word/meaning/sentence\n"
        "-- ============================================================\n\n"
        + gen_body
        + "\n"
    )

    # Insert before the anchor
    new_text = main_text.replace(INSERT_BEFORE_MARKER, block + INSERT_BEFORE_MARKER, 1)

    # Save as UTF-8 BOM
    with open(OUT_SQL, "wb") as f:
        f.write(b"\xef\xbb\xbf")  # BOM
        f.write(new_text.encode("utf-8"))

    size = os.path.getsize(OUT_SQL)
    print(f"OK -> {OUT_SQL}")
    print(f"   size: {size / 1024 / 1024:.2f} MB ({size} bytes)")
    print(f"   inserted {len(gen_body)} chars of generated SQL")


if __name__ == "__main__":
    main()
