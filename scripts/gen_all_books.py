"""
gen_all_books.py

Generates the SQL for filling up books 1 (CET4), 2 (CET6), 3 (KAOYAN),
6 (IELTS), 7 (ZHUANSHENG) with the words available in extracted_words.json.

Each book pulls from its primary source tag first, then top-ups from
fallback tags. The same word can appear in multiple books (with different
word_ids) - this is the cleanest way to fill all 5 books to a useful size
when the underlying dataset is small.
"""
import hashlib
import json
import os
import sys

# Make scripts folder importable
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, SCRIPT_DIR)

from image_pool import IMAGE_POOL
from word_to_category import get_category

PROJECT_DIR = os.path.dirname(SCRIPT_DIR)
WORDS_JSON = os.path.join(SCRIPT_DIR, "extracted_words.json")
OUTPUT_SQL = os.path.join(SCRIPT_DIR, "all_books.sql")


# Source priority list per book
BOOK_CONFIG = {
    1: {"primary": "CET4", "fallback": ["KAOYAN", "CET6", "IELTS"], "limit": 2600, "difficulty_mix": (0.30, 0.50, 0.20)},
    2: {"primary": "CET6", "fallback": ["KAOYAN", "IELTS"], "limit": 5500, "difficulty_mix": (0.20, 0.50, 0.30)},
    3: {"primary": "KAOYAN", "fallback": ["IELTS", "CET6"], "limit": 5500, "difficulty_mix": (0.15, 0.45, 0.40)},
    6: {"primary": "IELTS", "fallback": ["KAOYAN", "CET6"], "limit": 4500, "difficulty_mix": (0.15, 0.45, 0.40)},
    7: {"primary": "KAOYAN", "fallback": ["IELTS", "CET4", "CET6"], "limit": 3000, "difficulty_mix": (0.30, 0.55, 0.15)},
}


def clean_phonetic(p: str) -> str:
    """Convert kajweb style 'ə'bændən -> /əˈbændən/."""
    if not p:
        return ""
    s = str(p).strip()
    if not s:
        return ""
    # strip leading/trailing apostrophes
    s = s.strip("'")
    # internal ' -> ˈ
    s = s.replace("'", "\u02c8")
    return "/" + s + "/"


# MySQL column limits
MEANING_EN_MAX = 250
MEANING_ZH_MAX = 250
SENTENCE_MAX = 65000  # TEXT column


def _trunc(s: str, n: int) -> str:
    """Truncate to n chars, keep it safe and ASCII-clean."""
    if not s:
        return ""
    s = str(s)
    if len(s) <= n:
        return s
    return s[:n].rstrip()


def deterministic_image_url(word: str) -> str:
    """Pick a deterministic Unsplash URL based on word hash + category."""
    cat = get_category(word)
    urls = IMAGE_POOL.get(cat) or IMAGE_POOL["fallback"]
    h = int(hashlib.md5(word.lower().encode("utf-8")).hexdigest(), 16)
    return urls[h % len(urls)]


def pick_difficulty(word: str, mix: tuple) -> str:
    """Difficulty distribution: EASY 30%, NORMAL 50%, HARD 20% (or per-book mix)."""
    h = int(hashlib.md5(("d:" + word.lower()).encode("utf-8")).hexdigest(), 16)
    pct = (h % 1000) / 1000.0
    easy_pct, normal_pct, hard_pct = mix
    if pct < easy_pct:
        return "EASY"
    elif pct < easy_pct + normal_pct:
        return "NORMAL"
    else:
        return "HARD"


def sql_escape(s) -> str:
    if s is None:
        return ""
    s = str(s)
    # Escape backslash first, then single quote
    s = s.replace("\\", "\\\\")
    s = s.replace("'", "\\'")
    # Normalize newlines
    s = s.replace("\r", "").replace("\n", " ")
    return s


def get_best_entry(word: str, sources_priority: list, available_tags: set) -> dict:
    """Pick the first available source for this word from priority list."""
    for tag in sources_priority:
        if tag in available_tags:
            return {"tag": tag, "entry": word_data[word][tag]}
    return None


# Load words
print("Loading extracted_words.json ...")
with open(WORDS_JSON, "r", encoding="utf-8") as f:
    word_data = json.load(f)

print(f"Total unique words in dataset: {len(word_data)}")

# Build source mapping
all_source_words = {
    "CET4": [], "CET6": [], "KAOYAN": [], "IELTS": [],
}
for w, tags in word_data.items():
    for tag in all_source_words.keys():
        if tag in tags:
            all_source_words[tag].append(w)

for tag, words in all_source_words.items():
    print(f"  {tag}: {len(words)} words")

# Assign words to each book using the configured priority
print("\n=== Assigning words to books ===")
book_assignments = {}  # book_id -> list of (word, source_tag)
# Note: words CAN appear in multiple books with different word_ids.
# This is the cleanest way to fill all 5 books to a useful size
# when the underlying dataset is smaller than the sum of the limits.

for book_id, cfg in sorted(BOOK_CONFIG.items()):
    primary = cfg["primary"]
    fallbacks = cfg["fallback"] or []
    limit = cfg["limit"]

    chosen = []  # list of (word, source_tag) tuples

    # First, take all primary-source words (up to limit)
    for w in all_source_words.get(primary, []):
        chosen.append((w, primary))
        if len(chosen) >= limit:
            break

    # If not enough, top up from fallbacks in order
    if len(chosen) < limit:
        for fb in fallbacks:
            for w in all_source_words.get(fb, []):
                chosen.append((w, fb))
                if len(chosen) >= limit:
                    break
            if len(chosen) >= limit:
                break

    book_assignments[book_id] = chosen[:limit]
    print(f"  Book {book_id} ({cfg['primary']}): assigned {len(chosen[:limit])} words (target {limit})")

# Build the SQL
print("\n=== Building SQL ===")
word_rows = []  # tuples for INSERT INTO word
meaning_rows = []  # for word_meaning
sentence_rows = []  # for word_sentence
word_id_map = {}  # (book_id, word) -> word_id
next_word_id = 26  # start after the 25 example words

book_actual_counts = {}

for book_id, cfg in sorted(BOOK_CONFIG.items()):
    chosen = book_assignments[book_id]
    book_actual_counts[book_id] = len(chosen)
    mix = cfg["difficulty_mix"]
    for word, source_tag in chosen:
        entry = word_data[word][source_tag]
        usphone = clean_phonetic(entry.get("usphone", ""))
        ukphone = clean_phonetic(entry.get("ukphone", ""))
        # fall back to one or the other if empty
        if not usphone and ukphone:
            usphone = ukphone
        if not ukphone and usphone:
            ukphone = usphone
        if not usphone:
            usphone = "/"
            ukphone = "/"
        image_url = deterministic_image_url(word)
        difficulty = pick_difficulty(word, mix)
        wid = next_word_id
        next_word_id += 1
        word_id_map[(book_id, word)] = wid
        word_rows.append((wid, book_id, word, usphone, ukphone, image_url, difficulty))
        # meanings
        for idx, d in enumerate(entry.get("definitions", []), start=1):
            pos = _trunc(d.get("pos", ""), 16)
            zh = _trunc(d.get("zh", ""), MEANING_ZH_MAX)
            en = _trunc(d.get("en", ""), MEANING_EN_MAX)
            meaning_rows.append((wid, pos, zh, en, idx))
        # sentences
        for s in entry.get("sentences", [])[:2]:
            en_s = _trunc(s.get("en", ""), SENTENCE_MAX)
            zh_s = _trunc(s.get("zh", ""), SENTENCE_MAX)
            sentence_rows.append((wid, en_s, zh_s, "kajweb/dict"))

print(f"  word rows: {len(word_rows)}")
print(f"  meaning rows: {len(meaning_rows)}")
print(f"  sentence rows: {len(sentence_rows)}")

# Write SQL
print(f"\nWriting SQL to {OUTPUT_SQL}")
with open(OUTPUT_SQL, "w", encoding="utf-8") as f:
    # Header
    f.write("-- ============================================================\n")
    f.write("-- Auto-generated word data for books 1/2/3/6/7\n")
    f.write(f"-- Generated by gen_all_books.py\n")
    f.write(f"-- word rows: {len(word_rows)}\n")
    f.write(f"-- meaning rows: {len(meaning_rows)}\n")
    f.write(f"-- sentence rows: {len(sentence_rows)}\n")
    f.write("-- ============================================================\n\n")

    # Cleanup existing data for these books (in case of re-import)
    f.write("DELETE FROM `word_meaning` WHERE `word_id` IN (SELECT `id` FROM `word` WHERE `book_id` IN (1,2,3,6,7));\n")
    f.write("DELETE FROM `word_sentence` WHERE `word_id` IN (SELECT `id` FROM `word` WHERE `book_id` IN (1,2,3,6,7));\n")
    f.write("DELETE FROM `word` WHERE `book_id` IN (1,2,3,6,7);\n\n")

    # Update book word_count
    for book_id, count in book_actual_counts.items():
        f.write(f"UPDATE `book` SET `word_count`={count} WHERE `id`={book_id};\n")
    f.write("\n")

    # Build word INSERT batches
    BATCH = 300
    f.write("-- ===== word =====\n")
    for i in range(0, len(word_rows), BATCH):
        chunk = word_rows[i:i+BATCH]
        f.write("INSERT INTO `word` (`id`, `book_id`, `spelling`, `phonetic_us`, `phonetic_uk`, `image_url`, `difficulty`) VALUES\n")
        for j, row in enumerate(chunk):
            wid, bid, w, us, uk, img, diff = row
            w = sql_escape(w)
            us = sql_escape(us)
            uk = sql_escape(uk)
            img = sql_escape(img)
            diff = sql_escape(diff)
            sep = ",\n" if j < len(chunk) - 1 else ";\n"
            f.write(f"({wid},{bid},'{w}','{us}','{uk}','{img}','{diff}'){sep}")
        f.write("\n")

    # word_meaning INSERT
    f.write("\n-- ===== word_meaning =====\n")
    for i in range(0, len(meaning_rows), BATCH):
        chunk = meaning_rows[i:i+BATCH]
        f.write("INSERT INTO `word_meaning` (`word_id`,`pos`,`meaning_zh`,`meaning_en`,`sort`) VALUES\n")
        for j, row in enumerate(chunk):
            wid, pos, zh, en, sort = row
            pos = sql_escape(pos)
            zh = sql_escape(zh)
            en = sql_escape(en)
            sep = ",\n" if j < len(chunk) - 1 else ";\n"
            f.write(f"({wid},'{pos}','{zh}','{en}',{sort}){sep}")
        f.write("\n")

    # word_sentence INSERT
    f.write("\n-- ===== word_sentence =====\n")
    for i in range(0, len(sentence_rows), BATCH):
        chunk = sentence_rows[i:i+BATCH]
        f.write("INSERT INTO `word_sentence` (`word_id`,`en`,`zh`,`source`) VALUES\n")
        for j, row in enumerate(chunk):
            wid, en, zh, source = row
            en = sql_escape(en)
            zh = sql_escape(zh)
            source = sql_escape(source)
            sep = ",\n" if j < len(chunk) - 1 else ";\n"
            f.write(f"({wid},'{en}','{zh}','{source}'){sep}")
        f.write("\n")

print("Done!")
print(f"  -> {OUTPUT_SQL}")
print(f"  -> {os.path.getsize(OUTPUT_SQL) / 1024 / 1024:.2f} MB")
