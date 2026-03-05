"""find_missing_words.py

Loads the referenced-words map produced by extract_word_references.py,
builds the set of all spellings that already have entries in MongoDB,
and outputs two files:

1. data/missing_words.json  (full, gitignored — word → list of source refs)
2. data/missing_words_compact.json  (committed — filtered, sorted by word length)

Compact format:
  {
    "stats": {
      "existing_spellings": 42545,
      "unique_referenced":  98471,
      "missing":            65941,
      "after_filter":       58000
    },
    "missing_words": [
      ["সে", 5496],
      ["না",  93],
      ...
    ]
  }
  Array is sorted by word length ascending; ties broken by ref count descending.
  Entries containing no Bengali character are removed.

Run:
    python src/find_missing_words.py
    python src/find_missing_words.py --refs data/word_references.json \\
                                     --mongo mongodb://localhost:27017 \\
                                     --db Dictionary
"""

import argparse
import json
import sys
from pathlib import Path

from pymongo import MongoClient

sys.path.insert(0, str(Path(__file__).parent.parent))

from logger import logger

sys.stdout.reconfigure(encoding="utf-8")

# Bengali Unicode block: U+0980 – U+09FF
_BENGALI_START = 0x0980
_BENGALI_END   = 0x09FF


def has_bengali(text: str) -> bool:
    """Return True if text contains at least one Bengali Unicode character."""
    return any(_BENGALI_START <= ord(c) <= _BENGALI_END for c in text)


def load_existing_spellings(words_collection) -> set:
    """Return the set of all spellings currently in the Words collection."""
    spellings = set()
    for doc in words_collection.find({"status": "ACTIVE"}, {"spelling": 1, "_id": 0}):
        s = doc.get("spelling", "").strip()
        if s:
            spellings.add(s)
    logger.info(f"Loaded {len(spellings)} existing spellings from DB.")
    return spellings


def build_compact(missing: dict) -> list:
    """Return a filtered, length-sorted list of [word, ref_count] pairs.

    Filters out any entry that contains no Bengali character.
    Sorts by word length ascending, then ref count descending within same length.
    """
    pairs = []
    for word, refs in missing.items():
        if not word or not word.strip():
            continue
        if not has_bengali(word):
            continue
        pairs.append((word.strip(), len(refs)))

    pairs.sort(key=lambda p: (len(p[0]), -p[1]))
    return pairs


def main():
    parser = argparse.ArgumentParser(
        description="Diff referenced words against existing DB spellings."
    )
    parser.add_argument(
        "--refs",
        default="data/word_references.json",
        help="Path to word_references.json (output of extract_word_references.py)",
    )
    parser.add_argument(
        "--mongo",
        default="mongodb://localhost:27017",
        help="MongoDB connection URI (default: mongodb://localhost:27017)",
    )
    parser.add_argument(
        "--db", default="Dictionary", help="Database name (default: Dictionary)"
    )
    parser.add_argument(
        "--out",
        default="data/missing_words.json",
        help="Full output path (default: data/missing_words.json)",
    )
    parser.add_argument(
        "--out-compact",
        default="data/missing_words_compact.json",
        help="Compact output path (default: data/missing_words_compact.json)",
    )
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports only; skip MongoDB connection and file writes")
    args = parser.parse_args()

    if args.dry_run:
        logger.info(f"[DRY RUN] Imports OK. Would load {args.refs}, connect to {args.mongo}/{args.db}, and write outputs. No reads or writes.")
        return

    # --- load referenced words ---
    logger.info(f"Loading references from {args.refs} …")
    with open(args.refs, encoding="utf-8") as f:
        refs_data = json.load(f)
    referenced_words = refs_data["referenced_words"]
    logger.info(f"Loaded {len(referenced_words)} unique referenced tokens.")

    # --- load existing spellings ---
    client = MongoClient(args.mongo)
    collection = client[args.db]["Words"]
    existing = load_existing_spellings(collection)

    # --- diff ---
    missing = {
        word: refs
        for word, refs in referenced_words.items()
        if word not in existing
    }
    logger.info(
        f"Missing: {len(missing)} tokens "
        f"(out of {len(referenced_words)} referenced, {len(existing)} existing)."
    )

    # --- full output (gitignored) ---
    full_output = {
        "stats": {
            "existing_spellings": len(existing),
            "unique_referenced": len(referenced_words),
            "missing": len(missing),
        },
        "missing_words": missing,
    }
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(full_output, f, ensure_ascii=False, indent=2)
    logger.info(f"Full output written to {args.out}")

    # --- compact output (committed) ---
    compact_pairs = build_compact(missing)
    compact_output = {
        "stats": {
            "existing_spellings": len(existing),
            "unique_referenced": len(referenced_words),
            "missing": len(missing),
            "after_filter": len(compact_pairs),
        },
        "missing_words": compact_pairs,
    }
    with open(args.out_compact, "w", encoding="utf-8") as f:
        json.dump(compact_output, f, ensure_ascii=False, indent=2)
    logger.info(f"Compact output written to {args.out_compact}")

    # --- preview ---
    print("\nShortest 15 missing words:")
    print(f"  {'word':30s}  refs")
    print("  " + "-" * 40)
    for word, count in compact_pairs[:15]:
        print(f"  {word:30s}  {count}")

    print("\nTop 15 by ref count:")
    print(f"  {'word':30s}  refs")
    print("  " + "-" * 40)
    for word, count in sorted(compact_pairs, key=lambda p: -p[1])[:15]:
        print(f"  {word:30s}  {count}")


if __name__ == "__main__":
    main()
