"""Upload generated dictionary entries to the Shobdo API.

Reads data/generated_entries.json ({word: [entry, ...], ...}),
uploads each word and its meanings, and tracks progress in
data/upload_progress.json so runs can be resumed.
"""

import argparse
import sys
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from logger import logger
from utils import load_json, save_json
from word_client.client import WordClient, WordClientError

sys.stdout.reconfigure(encoding="utf-8")

SAVE_INTERVAL = 100


def upload_word(client: WordClient, word: str, entries: list):
    """Upload one word and all its meanings via WordClient. Returns (word, success_bool)."""
    try:
        word_record = client.get_or_create(word)
    except WordClientError as e:
        logger.error(f"Failed to create or fetch word '{word}': {e}")
        return word, False

    word_id = word_record["id"]
    for entry in entries:
        meaning = {
            "id": None,
            "text": entry.get("meaning", "").strip(),
            "partOfSpeech": entry.get("part_of_speech", ""),
            "strength": 0,
            "antonyms": entry.get("antonyms", []),
            "synonyms": entry.get("synonyms", []),
            "exampleSentence": entry.get("example_sentence", "").strip(),
        }
        try:
            client.add_meaning(word_id, meaning)
        except WordClientError as e:
            logger.error(f"Failed to add meaning for word id {word_id}: {e}")

    return word, True


def main():
    parser = argparse.ArgumentParser(description="Upload generated entries to Shobdo API")
    parser.add_argument("--input", default="data/generated_entries.json")
    parser.add_argument("--progress", default="data/upload_progress.json")
    parser.add_argument("--api-base", default="http://localhost:9000")
    parser.add_argument("--limit", type=int, default=0, help="0 = upload all")
    parser.add_argument("--threads", type=int, default=20)
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports and input file only; skip all API calls and writes")
    args = parser.parse_args()

    if args.dry_run:
        logger.info(f"[DRY RUN] Imports OK. Would upload words from {args.input} to {args.api_base}. No API calls or writes.")
        return

    generated: dict = load_json(args.input, {})
    if not generated:
        logger.error(f"No data found in {args.input}")
        return

    uploaded: list = load_json(args.progress, [])
    uploaded_set = set(uploaded)
    logger.info(f"Loaded {len(generated)} words | already uploaded: {len(uploaded_set)}")

    words_to_upload = [w for w in generated if w not in uploaded_set]
    if args.limit > 0:
        words_to_upload = words_to_upload[:args.limit]

    total = len(words_to_upload)
    logger.info(f"Words to upload: {total} | threads: {args.threads}")

    lock = threading.Lock()
    done = 0
    errors = 0

    with WordClient(args.api_base) as client, ThreadPoolExecutor(max_workers=args.threads) as executor:
        futures = {
            executor.submit(upload_word, client, w, generated[w]): w
            for w in words_to_upload
        }
        for future in as_completed(futures):
            word, success = future.result()
            with lock:
                done += 1
                if success:
                    uploaded_set.add(word)
                else:
                    errors += 1
                if done % SAVE_INTERVAL == 0:
                    save_json(args.progress, list(uploaded_set))
                    logger.info(f"[{done}/{total}] uploaded: {len(uploaded_set)} | errors: {errors}")

    save_json(args.progress, list(uploaded_set))
    logger.info(f"Done — uploaded: {len(uploaded_set)} | errors: {errors}")


if __name__ == "__main__":
    main()
