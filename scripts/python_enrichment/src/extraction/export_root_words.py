"""
export_root_words.py — Export all root words from MongoDB to a JSON file.

A "root word" is an ACTIVE word in the Words collection that does not appear
in the InflectionIndex collection (i.e. it is not an inflected form of another
word).  When no inflection data has been loaded yet the two sets are identical,
so this script is safe to run at any time.

Output file: data/export/root_words_<YYYY-MM-DD_HH-MM-SS>.json

Schema:
  {
    "meta": {
      "exported_at": "2026-03-20T14:30:00",
      "mongo_uri":   "mongodb://localhost:27017",
      "db":          "Dictionary",
      "count":       107943
    },
    "words": [
      {"id": "WD-...", "spelling": "শব্দ"},
      ...
    ]
  }

Usage:
    # Default — all root words, id + spelling only
    poetry run python src/extraction/export_root_words.py

    # Custom Mongo URI / DB
    poetry run python src/extraction/export_root_words.py \\
        --mongo mongodb://localhost:27017 --db Dictionary

    # Limit for testing
    poetry run python src/extraction/export_root_words.py --limit 500

    # Write to a specific path instead of auto-named file
    poetry run python src/extraction/export_root_words.py --out data/export/my_words.json
"""

from __future__ import annotations

import argparse
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from pymongo import MongoClient

from src.logger import setup_logger
from src.utils import save_json

logger = setup_logger("export_root_words")

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parents[3]
EXPORT_DIR = REPO_ROOT / "scripts" / "python_enrichment" / "data" / "export"


def fetch_root_words(mongo_uri: str, db_name: str, limit: int = 0) -> list[dict]:
    """
    Return all ACTIVE words that are not recorded as inflections.

    Uses the InflectionIndex collection to exclude known inflected forms.
    When that collection is empty (no inflection data loaded yet) every
    ACTIVE word is returned.
    """
    with MongoClient(mongo_uri) as client:
        db = client[db_name]

        # Collect all spellings that are inflections (fast O(n) set build)
        inflection_spellings: set[str] = {
            doc["spelling"]
            for doc in db["InflectionIndex"].find({}, {"spelling": 1, "_id": 0})
        }
        logger.info(f"InflectionIndex entries: {len(inflection_spellings):,}")

        cursor = db["Words"].find(
            {"status": "ACTIVE"},
            {"id": 1, "spelling": 1, "_id": 0},
        )

        words: list[dict] = []
        for doc in cursor:
            if doc["spelling"] not in inflection_spellings:
                words.append({"id": doc["id"], "spelling": doc["spelling"]})
            if limit > 0 and len(words) >= limit:
                break

    logger.info(f"Root words found: {len(words):,}")
    return words


def export(
    mongo_uri: str,
    db_name: str,
    out_path: Path | None = None,
    limit: int = 0,
) -> Path:
    words = fetch_root_words(mongo_uri, db_name, limit=limit)

    now = datetime.now()
    timestamp = now.strftime("%Y-%m-%d_%H-%M-%S")

    if out_path is None:
        EXPORT_DIR.mkdir(parents=True, exist_ok=True)
        out_path = EXPORT_DIR / f"root_words_{timestamp}.json"

    payload = {
        "meta": {
            "exported_at": now.isoformat(timespec="seconds"),
            "mongo_uri": mongo_uri,
            "db": db_name,
            "count": len(words),
        },
        "words": words,
    }

    save_json(out_path, payload)
    logger.info(f"Saved {len(words):,} root words → {out_path}")
    return out_path


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Export all root words from MongoDB to a timestamped JSON file"
    )
    parser.add_argument(
        "--mongo",
        default="mongodb://localhost:27017",
        help="MongoDB connection URI (default: mongodb://localhost:27017)",
    )
    parser.add_argument(
        "--db",
        default="Dictionary",
        help="MongoDB database name (default: Dictionary)",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=0,
        help="Max words to export (0 = all, useful for testing)",
    )
    parser.add_argument(
        "--out",
        default="",
        help="Output file path. If omitted, auto-named under data/export/",
    )
    args = parser.parse_args()

    out_path = Path(args.out) if args.out else None
    try:
        result_path = export(
            mongo_uri=args.mongo,
            db_name=args.db,
            out_path=out_path,
            limit=args.limit,
        )
        print(result_path)
    except Exception as e:
        logger.error(str(e))
        sys.exit(1)


if __name__ == "__main__":
    main()
