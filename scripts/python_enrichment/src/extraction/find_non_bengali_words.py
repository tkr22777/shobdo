"""
find_non_bengali_words.py — Find, save, and optionally delete words with foreign-script characters.

Flags characters belonging to foreign scripts (Devanagari/Hindi, Arabic, Kannada,
Gurmukhi/Punjabi, etc.).  Bengali characters, Latin letters, digits, and all
common punctuation (space, hyphen, comma, dot, parentheses, quotes, etc.) are
all considered acceptable in Bengali writing and are NOT flagged.

Saves flagged words to:
    data/export/foreign_script_words_<timestamp>.json

Then optionally soft-deletes them via the REST API (DELETE /api/v1/words/:id).

Output file schema:
  {
    "meta": {
      "found_at": "...", "input_file": "...", "count": 35,
      "deleted": false
    },
    "words": [
      {"id": "WD-...", "spelling": "কৃমिज", "offending": [{"char": "ि", "codepoint": "U+093F", "script": "DEVANAGARI"}]},
      ...
    ]
  }

Usage:
    # Find and save (no deletions)
    poetry run python src/extraction/find_non_bengali_words.py

    # Find, save, then delete from local DB via API
    poetry run python src/extraction/find_non_bengali_words.py --delete

    # Dry-run delete (shows what would be deleted without doing it)
    poetry run python src/extraction/find_non_bengali_words.py --delete --dry-run

    # Explicit input file
    poetry run python src/extraction/find_non_bengali_words.py \\
        --input data/export/root_words_2026-03-20_15-39-04.json
"""

from __future__ import annotations

import argparse
import sys
import unicodedata
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from pymongo import MongoClient

from src.logger import setup_logger
from src.utils import load_json, save_json

logger = setup_logger("find_non_bengali_words")

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parents[3]
EXPORT_DIR = REPO_ROOT / "scripts" / "python_enrichment" / "data" / "export"

_FOREIGN_SCRIPT_PREFIXES = (
    "DEVANAGARI",   # Hindi
    "ARABIC",
    "KANNADA",
    "GURMUKHI",     # Punjabi
    "TAMIL",
    "TELUGU",
    "MALAYALAM",
    "ORIYA",
    "SINHALA",
    "TIBETAN",
    "MYANMAR",
    "HANGUL",
    "CJK",
    "HIRAGANA",
    "KATAKANA",
    "THAI",
    "HEBREW",
    "CYRILLIC",
    "GREEK",
)


def _script_of(ch: str) -> str | None:
    name = unicodedata.name(ch, "")
    for prefix in _FOREIGN_SCRIPT_PREFIXES:
        if name.startswith(prefix):
            return prefix
    return None


def foreign_chars(spelling: str) -> list[dict]:
    """Return unique foreign-script characters as {char, codepoint, script} dicts."""
    seen: set[str] = set()
    result: list[dict] = []
    for ch in spelling:
        if ch not in seen:
            script = _script_of(ch)
            if script:
                seen.add(ch)
                result.append({
                    "char": ch,
                    "codepoint": f"U+{ord(ch):04X}",
                    "script": script,
                })
    return result


def latest_export() -> Path:
    files = sorted(EXPORT_DIR.glob("root_words_*.json"), reverse=True)
    if not files:
        raise FileNotFoundError(f"No root_words_*.json files found in {EXPORT_DIR}")
    return files[0]


def find_flagged(input_path: Path) -> list[dict]:
    data = load_json(input_path)
    flagged = []
    for word in data["words"]:
        bad = foreign_chars(word["spelling"])
        if bad:
            flagged.append({**word, "offending": bad})
    return flagged


def save_flagged(flagged: list[dict], input_path: Path, deleted: bool) -> Path:
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    out_path = EXPORT_DIR / f"foreign_script_words_{timestamp}.json"
    save_json(out_path, {
        "meta": {
            "found_at": datetime.now().isoformat(timespec="seconds"),
            "input_file": str(input_path),
            "count": len(flagged),
            "deleted": deleted,
        },
        "words": flagged,
    })
    logger.info(f"Saved {len(flagged)} flagged words → {out_path}")
    return out_path


def delete_words(flagged: list[dict], mongo_uri: str, db_name: str, dry_run: bool) -> None:
    """Permanently remove flagged words from MongoDB by their `id` field."""
    if dry_run:
        logger.info(f"[DRY RUN] Would permanently delete {len(flagged)} words from {mongo_uri}/{db_name}")
        for w in flagged:
            print(f"  would delete: {w['spelling']} ({w['id']})")
        return

    with MongoClient(mongo_uri) as client:
        collection = client[db_name]["Words"]
        deleted = 0
        failed = 0
        for w in flagged:
            result = collection.delete_one({"id": w["id"]})
            if result.deleted_count == 1:
                logger.info(f"Deleted: {w['spelling']} ({w['id']})")
                deleted += 1
            else:
                logger.warning(f"Not found in DB: {w['spelling']} ({w['id']})")
                failed += 1

    logger.info(f"Done — deleted: {deleted}, not found: {failed}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Find words with foreign-script characters, save to file, optionally delete"
    )
    parser.add_argument(
        "--input",
        default="",
        help="Path to root_words JSON file. Defaults to the most recent file in data/export/",
    )
    parser.add_argument(
        "--delete",
        action="store_true",
        help="Soft-delete flagged words via the REST API after saving",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="With --delete: show what would be deleted without doing it",
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
    args = parser.parse_args()

    input_path = Path(args.input) if args.input else latest_export()
    logger.info(f"Reading from: {input_path}")

    try:
        flagged = find_flagged(input_path)
        logger.info(f"Foreign-script words found: {len(flagged)}")

        for w in flagged:
            chars_desc = ", ".join(
                f"'{c['char']}' {c['codepoint']} ({c['script']})"
                for c in w["offending"]
            )
            print(f"{w['spelling']}  →  [{chars_desc}]")

        out_path = save_flagged(flagged, input_path, deleted=False)
        print(f"\nSaved to: {out_path}")

        if args.delete:
            delete_words(flagged, args.mongo, args.db, dry_run=args.dry_run)
            if not args.dry_run:
                # Re-save with deleted=True
                save_flagged(flagged, input_path, deleted=True)

        print(f"\nTotal flagged: {len(flagged):,}")

    except Exception as e:
        logger.error(str(e))
        sys.exit(1)


if __name__ == "__main__":
    main()
