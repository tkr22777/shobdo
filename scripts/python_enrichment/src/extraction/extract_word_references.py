"""extract_word_references.py

Scans every word in the Dictionary MongoDB collection and builds a map of
every word token referenced in synonyms, antonyms, and example sentences,
with a back-pointer to the source word + meaning + field.

Output: data/word_references.json
  {
    "stats": {
      "words_scanned": 42545,
      "unique_referenced": 61234
    },
    "referenced_words": {
      "শব্দ": [
        {
          "word_id":       "WD-...",
          "word_spelling": "...",
          "meaning_id":    "MNG-...",
          "field":         "synonym" | "antonym" | "exampleSentence"
        },
        ...
      ]
    }
  }

Run:
    python src/extract_word_references.py
    python src/extract_word_references.py --mongo mongodb://localhost:27017 --db Dictionary
"""

import argparse
import json
import re
import sys
from collections import defaultdict
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Dict, List

from pymongo import MongoClient

sys.path.insert(0, str(Path(__file__).parent.parent))

from logger import logger

sys.stdout.reconfigure(encoding="utf-8")

# ---------------------------------------------------------------------------
# Tokenisation
# ---------------------------------------------------------------------------

# Strip Bengali/ASCII punctuation and digits before splitting on whitespace.
_STRIP = re.compile(r"[।,!?;:\"'()৷.\-–—০-৯0-9\[\]{}/<>@#$%^&*+=|\\`~]")


def tokenize(text: str) -> List[str]:
    """Return individual word tokens from a Bengali string.

    Strips punctuation and digits, splits on whitespace, and drops
    single-character tokens (most are particles or stray punctuation).
    """
    if not text:
        return []
    cleaned = _STRIP.sub(" ", text)
    return [t for t in cleaned.split() if len(t) > 1]


# ---------------------------------------------------------------------------
# Source reference dataclass
# ---------------------------------------------------------------------------


@dataclass
class SourceRef:
    word_id: str
    word_spelling: str
    field: str  # 'synonym' | 'antonym' | 'exampleSentence'


# ---------------------------------------------------------------------------
# Parsing
# ---------------------------------------------------------------------------


def extract_references(
    words_collection,
    limit: int = 0,
) -> Dict[str, List[SourceRef]]:
    """Stream all active Word documents and build the referenced-words map.

    Returns:
        referenced_words: dict mapping each extracted token → list of SourceRef
    """
    referenced_words: Dict[str, List[SourceRef]] = defaultdict(list)
    words_scanned = 0

    cursor = words_collection.find({"status": "ACTIVE"})
    if limit:
        cursor = cursor.limit(limit)

    for doc in cursor:
        doc.pop("_id", None)
        word_id = doc.get("id", "")
        word_spelling = doc.get("spelling", "")
        meanings = doc.get("meanings") or {}

        for meaning_id, meaning in meanings.items():
            if not isinstance(meaning, dict):
                continue

            # --- synonyms: keep full phrase AND add each individual token ---
            for phrase in meaning.get("synonyms") or []:
                phrase = phrase.strip()
                if len(phrase) > 1:
                    referenced_words[phrase].append(
                        SourceRef(word_id, word_spelling, "synonym")
                    )
                for token in tokenize(phrase):
                    if token != phrase:  # avoid double-adding single-word phrases
                        referenced_words[token].append(
                            SourceRef(word_id, word_spelling, "synonym")
                        )

            # --- antonyms: keep full phrase AND add each individual token ---
            for phrase in meaning.get("antonyms") or []:
                phrase = phrase.strip()
                if len(phrase) > 1:
                    referenced_words[phrase].append(
                        SourceRef(word_id, word_spelling, "antonym")
                    )
                for token in tokenize(phrase):
                    if token != phrase:
                        referenced_words[token].append(
                            SourceRef(word_id, word_spelling, "antonym")
                        )

            # --- example sentence (needs tokenisation) ---
            for token in tokenize(meaning.get("exampleSentence") or ""):
                referenced_words[token].append(
                    SourceRef(word_id, word_spelling, "exampleSentence")
                )

        words_scanned += 1
        if words_scanned % 5000 == 0:
            logger.info(f"Scanned {words_scanned} words …")

    logger.info(f"Done. Scanned {words_scanned} words, "
                f"found {len(referenced_words)} unique referenced tokens.")
    return referenced_words, words_scanned


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------


def main():
    parser = argparse.ArgumentParser(description="Extract word references from MongoDB.")
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
        default="data/word_references.json",
        help="Output JSON file path (default: data/word_references.json)",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=0,
        help="Limit number of words scanned (0 = all, useful for testing)",
    )
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports only; skip MongoDB connection and file writes")
    args = parser.parse_args()

    if args.dry_run:
        logger.info(f"[DRY RUN] Imports OK. Would connect to {args.mongo}/{args.db} and write to {args.out}. No reads or writes.")
        return

    logger.info(f"Connecting to {args.mongo} / {args.db} …")
    client = MongoClient(args.mongo)
    collection = client[args.db]["Words"]

    referenced_words, words_scanned = extract_references(collection, limit=args.limit)

    # Serialise: convert SourceRef dataclasses → dicts, sort by ref count desc
    output = {
        "stats": {
            "words_scanned": words_scanned,
            "unique_referenced": len(referenced_words),
        },
        "referenced_words": {
            word: [asdict(ref) for ref in refs]
            for word, refs in sorted(
                referenced_words.items(), key=lambda kv: len(kv[1]), reverse=True
            )
        },
    }

    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)

    logger.info(f"Written to {args.out}")


if __name__ == "__main__":
    main()
