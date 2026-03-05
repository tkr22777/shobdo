"""discovery/generate_prefix_sets.py

Builds an ordered, prefix-grouped set list from a Bangla word list.
Each set covers a contiguous alphabetical slice (grouped by first character).
Large first-character groups are split into chunks of --set-size.
The first word of set[i+1] is appended to set[i] for lexical continuity,
so an LLM processing set[i] can see the boundary with the next group.

Run from scripts/python_enrichment/:
    # From existing dictionary (requires MongoDB)
    poetry run python src/discovery/generate_prefix_sets.py --source db

    # From a word-list file
    poetry run python src/discovery/generate_prefix_sets.py --source file \
        --words-file data/missing_words_compact.json

    # Preview 2 small sets without saving
    poetry run python src/discovery/generate_prefix_sets.py --set-size 8 --preview 2

Output: data/discovery/prefix_sets.json
    {
      "meta": { "total_sets": 466, "total_words": 65563, "set_size": 150, ... },
      "sets": [
        {
          "index": 0,
          "prefix": "অ",
          "words": ["অকথ্য", ..., "<first word of next set>"],
          "word_count": 150,          <- excludes continuity word
          "has_continuity_word": true
        },
        ...
      ]
    }

The last word in each set's `words` (when has_continuity_word=true) is the first
word of the next set — boundary context only, not part of this set's range.
"""

import argparse
import json
import sys
from pathlib import Path

# Allow imports of config / logger from parent src/ directory
sys.path.insert(0, str(Path(__file__).parent.parent))

from logger import logger

sys.stdout.reconfigure(encoding="utf-8")

_BANGLA_START = 0x0980
_BANGLA_END   = 0x09FF

# Valid word-initial Bangla characters (vowels + consonants + special letters).
# Excludes vowel marks (matras), digits, punctuation, and combining marks.
_VALID_INITIALS = (
    set(range(0x0985, 0x0995))   # অ–ঔ  (independent vowels)
    | set(range(0x0995, 0x09BA)) # ক–হ  (consonants)
    | {0x09CE, 0x09DC, 0x09DD, 0x09DF}  # ৎ ড় ঢ় য়
)


def is_valid_initial(ch: str) -> bool:
    return ord(ch) in _VALID_INITIALS


def first_bangla_char(word: str) -> str | None:
    """Return the first Bangla character that is a valid word-initial, or None."""
    for ch in word:
        if 0x0980 <= ord(ch) <= 0x09FF:
            return ch if is_valid_initial(ch) else None
    return None


def load_words_from_file(path: str) -> list[str]:
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    if isinstance(data, dict) and "missing_words" in data:
        return [w for w, _ in data["missing_words"]]
    if isinstance(data, list):
        if data and isinstance(data[0], list):
            return [w for w, *_ in data]
        return data
    raise ValueError(f"Unrecognised format in {path}")


def load_words_from_db(mongo_uri: str, db_name: str) -> list[str]:
    from pymongo import MongoClient
    client = MongoClient(mongo_uri)
    col = client[db_name]["Words"]
    words = []
    for doc in col.find({"status": "ACTIVE"}, {"spelling": 1, "_id": 0}):
        s = doc.get("spelling", "").strip()
        if s:
            words.append(s)
    logger.info(f"Loaded {len(words)} words from MongoDB.")
    return words


def build_prefix_sets(words: list[str], set_size: int) -> list[dict]:
    """Sort, filter, group by first Bangla character, split, and apply continuity."""
    # Deduplicate, filter junk initials, sort (Bangla Unicode = Python default order)
    filtered = sorted(set(
        w for w in words
        if w and first_bangla_char(w) is not None
    ))
    dropped = len(words) - len(filtered)
    if dropped:
        logger.info(f"Filtered out {dropped} words with invalid/non-Bangla initials.")

    # Group by first valid Bangla character
    groups: dict[str, list[str]] = {}
    for word in filtered:
        key = first_bangla_char(word)
        groups.setdefault(key, []).append(word)

    # Split each group into chunks of set_size
    chunks: list[list[str]] = []
    for key in sorted(groups.keys()):
        group = groups[key]
        for i in range(0, len(group), set_size):
            chunks.append(group[i : i + set_size])

    # Apply continuity: last element of chunks[i] = first word of chunks[i+1]
    sets = []
    for i, chunk in enumerate(chunks):
        has_cont = i + 1 < len(chunks)
        sets.append({
            "index": i,
            "prefix": first_bangla_char(chunk[0]),
            "words": chunk + ([chunks[i + 1][0]] if has_cont else []),
            "word_count": len(chunk),
            "has_continuity_word": has_cont,
            # Lexicographic bounds of the core words in this set (excludes continuity word).
            # Use these to constrain LLM suggestions to this exact alphabetical slice.
            "range_start": chunk[0],
            "range_end": chunk[-1],
        })

    return sets


def preview_sets(sets: list[dict], n: int):
    for s in sets[:n]:
        core = s["words"][:-1] if s["has_continuity_word"] else s["words"]
        cont = s["words"][-1] if s["has_continuity_word"] else None
        print(f"\n{'─'*60}")
        print(f"Set #{s['index']}  prefix='{s['prefix']}'  words={s['word_count']}")
        print(f"  Core words ({len(core)}): {core}")
        if cont:
            print(f"  Continuity word (boundary, not part of set): '{cont}'")


def main():
    parser = argparse.ArgumentParser(
        description="Generate prefix-grouped Bangla word sets for LLM gap discovery"
    )
    parser.add_argument("--source", choices=["file", "db"], default="file")
    parser.add_argument("--words-file", default="data/missing_words_compact.json")
    parser.add_argument("--mongo", default="mongodb://localhost:27017")
    parser.add_argument("--db", default="Dictionary")
    parser.add_argument("--set-size", type=int, default=75)
    parser.add_argument("--out", default="data/discovery/prefix_sets.json")
    parser.add_argument("--preview", type=int, default=0,
                        help="Print first N sets to stdout and exit without saving")
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports only; skip loading words and file writes")
    args = parser.parse_args()

    if args.dry_run:
        logger.info(f"[DRY RUN] Imports OK. Would load words from {args.source} and write sets to {args.out}. No reads or writes.")
        return

    words = (load_words_from_db(args.mongo, args.db)
             if args.source == "db"
             else load_words_from_file(args.words_file))
    logger.info(f"Loaded {len(words)} words from {args.source}.")

    sets = build_prefix_sets(words, args.set_size)
    logger.info(f"Generated {len(sets)} sets (set_size={args.set_size}).")

    if args.preview > 0:
        preview_sets(sets, args.preview)
        return

    Path(args.out).parent.mkdir(parents=True, exist_ok=True)
    output = {
        "meta": {
            "total_sets": len(sets),
            "total_words": sum(s["word_count"] for s in sets),
            "set_size": args.set_size,
            "source": args.source,
            "words_file": args.words_file if args.source == "file" else None,
        },
        "sets": sets,
    }
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    logger.info(f"Saved to {args.out}.")

    from collections import Counter
    prefix_counts = Counter(s["prefix"] for s in sets)
    logger.info("Sets per prefix:")
    for ch in sorted(prefix_counts, key=ord):
        logger.info(f"  {ch}: {prefix_counts[ch]} set(s) "
                    f"({sum(s['word_count'] for s in sets if s['prefix']==ch)} words)")


if __name__ == "__main__":
    main()
