"""discovery/query_prefix_sets.py

Feeds each prefix set to Gemini and asks it to identify Bangla words that
should be in a complete dictionary but are missing from that set.

Run from scripts/python_enrichment/:
    # Preview prompt for set #0 without calling the API
    poetry run python src/discovery/query_prefix_sets.py --preview-prompt 0

    # Test run: query first 3 sets
    poetry run python src/discovery/query_prefix_sets.py --limit 3

    # Full run (resumable)
    poetry run python src/discovery/query_prefix_sets.py

Input:   data/discovery/prefix_sets.json
Output:  data/discovery/discovered_words.json
Progress: data/discovery/query_progress.json  (set of completed set indices)

Output format:
    {
      "meta": { "model": "...", "sets_queried": 42, ... },
      "results": {
        "0": { "prefix": "অ", "suggested": ["অকিঞ্চন", ...], "raw_count": 14 },
        "7": { "prefix": "আ", "suggested": [...], ... },
        ...
      }
    }
"""

import argparse
import json
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import google.generativeai as genai

sys.path.insert(0, str(Path(__file__).parent.parent))

from config import settings
from logger import logger

sys.stdout.reconfigure(encoding="utf-8")

genai.configure(api_key=settings.GEMINI_API_KEY)

# ── Model ─────────────────────────────────────────────────────────────────────
MODEL = "models/gemini-2.5-flash-lite"

# ── Prompt ────────────────────────────────────────────────────────────────────

SYSTEM_PROMPT = """\
You are a Bangla lexicographer reviewing a dictionary for completeness.

You will be given a sorted (lexicographic) slice of Bangla words, all starting with
the same prefix character. The slice has an explicit alphabetical range:
  range_start — the first word of this slice
  range_end   — the last word of this slice

Your task is to identify Bangla words that:
1. Genuinely exist in the Bangla language (common, literary, archaic, technical,
   colloquial, or dialectal)
2. Would be expected in a comprehensive Bangla dictionary
3. Are NOT already present in the provided list

━━━ RANGE CONSTRAINT ━━━

Only suggest words that fall alphabetically between range_start and range_end
(inclusive). This is one alphabetical slice of the dictionary — words outside
this range belong to a different slice.

━━━ WHAT TO SUGGEST ━━━

Focus on:
- Root / base forms of words (suggest "অকিঞ্চন", not "অকিঞ্চনের")
- Words a literate Bangla speaker would recognise
- Both high-frequency everyday words and less common but legitimate entries
- Verb roots, noun forms, adjectives, particles, fixed expressions

Do NOT suggest:
- Any word already in the provided list
- Proper nouns: names of people, places, organisations, brands
- Purely inflected or derived forms whose root is already in the list
  (e.g. do not suggest "গানটি" if "গান" is already present)
- Transliterations that are not yet adopted into Bangla (no Roman letters)
- Invented, speculative, or extremely obscure words with no established usage

━━━ OUTPUT FORMAT ━━━

Return ONLY this JSON — no explanation, no markdown:
{
  "prefix": "<the prefix character>",
  "suggested": ["word1", "word2", ...],
  "count": <integer>
}

Be concise. Only include words you are confident exist in Bangla.
Stop as soon as you run out of genuine missing entries — do not pad or repeat.
If the section is well-covered, return an empty suggested list.
"""


def build_user_prompt(prefix: str, words: list[str], range_start: str, range_end: str) -> str:
    """Build the per-set user message from the prefix, word list, and lexicographic range."""
    word_list = ", ".join(f'"{w}"' for w in words)
    return (
        f'Prefix: "{prefix}"\n'
        f'Range: "{range_start}" … "{range_end}" (suggest only words within this range)\n\n'
        f"Existing words in this section ({len(words)} words):\n"
        f"[{word_list}]\n\n"
        f'What Bangla words starting with "{prefix}" that fall between '
        f'"{range_start}" and "{range_end}" are missing from this list?'
    )


# ── Gemini call ───────────────────────────────────────────────────────────────

def query_set(set_index: int, prefix: str, words: list[str],
              range_start: str, range_end: str) -> tuple[int, dict]:
    """Query Gemini for one prefix set. Returns (set_index, result_dict)."""
    model = genai.GenerativeModel(MODEL)
    generation_config = genai.types.GenerationConfig(
        temperature=0.2,
        response_mime_type="application/json",
    )
    safety_settings = {
        "HARM_CATEGORY_HARASSMENT": "BLOCK_NONE",
        "HARM_CATEGORY_HATE_SPEECH": "BLOCK_NONE",
        "HARM_CATEGORY_SEXUALLY_EXPLICIT": "BLOCK_NONE",
        "HARM_CATEGORY_DANGEROUS_CONTENT": "BLOCK_NONE",
    }

    user_prompt = build_user_prompt(prefix, words, range_start, range_end)

    try:
        response = model.generate_content(
            contents=[SYSTEM_PROMPT, user_prompt],
            generation_config=generation_config,
            safety_settings=safety_settings,
        )

        if not response.candidates or not response.candidates[0].content:
            logger.error(f"[Set #{set_index}] Empty response from Gemini.")
            return set_index, {"error": "empty_response", "prefix": prefix}

        raw = response.text
    except Exception as e:
        logger.error(f"[Set #{set_index}] Gemini API error: {e}")
        return set_index, {"error": str(e), "prefix": prefix}

    try:
        data = json.loads(raw)
    except json.JSONDecodeError as e:
        logger.error(f"[Set #{set_index}] JSON parse error: {e} | raw: {raw[:150]}")
        return set_index, {"error": f"json_parse: {e}", "prefix": prefix}

    suggested = list(dict.fromkeys(data.get("suggested", [])))  # deduplicate, preserve order
    return set_index, {
        "prefix": prefix,
        "suggested": suggested,
        "raw_count": len(suggested),
    }


# ── I/O helpers ───────────────────────────────────────────────────────────────

def load_json(path: str, default):
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError:
        return default


def save_json(path: str, data):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Query Gemini per prefix set to discover missing Bangla words"
    )
    parser.add_argument("--sets-file", default="data/discovery/prefix_sets.json")
    parser.add_argument("--out", default="data/discovery/discovered_words.json")
    parser.add_argument("--progress", default="data/discovery/query_progress.json")
    parser.add_argument("--limit", type=int, default=0,
                        help="Max sets to query this run (0 = all)")
    parser.add_argument("--threads", type=int, default=10,
                        help="Parallel Gemini calls (default: 10)")
    parser.add_argument("--preview-prompt", type=int, default=-1, metavar="SET_INDEX",
                        help="Print the prompt for a set index and exit (no API call)")
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports and sets file only; skip Gemini calls and writes")
    args = parser.parse_args()

    if args.dry_run:
        logger.info(f"[DRY RUN] Imports OK. Would query Gemini ({MODEL}) across sets in {args.sets_file}. No API calls or writes.")
        return

    # Load prefix sets
    sets_data = load_json(args.sets_file, None)
    if not sets_data:
        logger.error(f"Could not load {args.sets_file}. Run generate_prefix_sets.py first.")
        sys.exit(1)
    all_sets = sets_data["sets"]

    # Preview mode — just print the prompt for one set
    if args.preview_prompt >= 0:
        idx = args.preview_prompt
        if idx >= len(all_sets):
            logger.error(f"Set #{idx} does not exist (total: {len(all_sets)})")
            sys.exit(1)
        s = all_sets[idx]
        # Pass core words only (exclude continuity word)
        core = s["words"][:-1] if s["has_continuity_word"] else s["words"]
        print("═" * 70)
        print(f"SYSTEM PROMPT:\n{SYSTEM_PROMPT}")
        print("═" * 70)
        print(f"USER PROMPT (Set #{idx}):\n{build_user_prompt(s['prefix'], core, s['range_start'], s['range_end'])}")
        print("═" * 70)
        return

    # Load progress and existing results
    completed: list = load_json(args.progress, [])
    completed_set = set(completed)
    results: dict = load_json(args.out, {}).get("results", {})

    logger.info(
        f"Loaded {len(all_sets)} sets | "
        f"already done: {len(completed_set)} | "
        f"remaining: {len(all_sets) - len(completed_set)}"
    )

    # Build work list
    to_query = [s for s in all_sets if s["index"] not in completed_set]
    if args.limit > 0:
        to_query = to_query[: args.limit]

    total = len(to_query)
    logger.info(f"Sets to query this run: {total} | threads: {args.threads}")

    if not total:
        logger.info("Nothing to do.")
        return

    log_interval  = max(1, min(50, total // 10))
    save_interval = max(10, min(100, total // 5))

    processed = 0
    errors = 0
    total_suggested = 0

    with ThreadPoolExecutor(max_workers=args.threads) as executor:
        futures = {
            executor.submit(
                query_set,
                s["index"],
                s["prefix"],
                s["words"][:-1] if s["has_continuity_word"] else s["words"],
                s["range_start"],
                s["range_end"],
            ): s["index"]
            for s in to_query
        }

        for future in as_completed(futures):
            set_idx = futures[future]
            try:
                idx, result = future.result()
            except Exception as e:
                logger.error(f"Unhandled exception for set #{set_idx}: {e}")
                errors += 1
                processed += 1
                continue

            if "error" in result:
                errors += 1
            else:
                results[str(idx)] = result
                completed_set.add(idx)
                total_suggested += result.get("raw_count", 0)

            processed += 1

            if processed % log_interval == 0 or processed == total:
                logger.info(
                    f"[{processed}/{total}] sets done | "
                    f"suggested so far: {total_suggested} | errors: {errors}"
                )

            if processed % save_interval == 0:
                save_json(args.out, {"meta": {"model": MODEL}, "results": results})
                save_json(args.progress, list(completed_set))
                logger.info("Progress saved.")

    # Final save
    save_json(args.out, {"meta": {"model": MODEL, "sets_queried": len(results)}, "results": results})
    save_json(args.progress, list(completed_set))

    logger.info(
        f"Done — sets queried: {len(results)} | "
        f"total words suggested: {total_suggested} | errors: {errors}"
    )


if __name__ == "__main__":
    main()
