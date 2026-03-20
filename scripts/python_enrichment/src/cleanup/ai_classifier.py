"""
ai_classifier.py — Classify words as VALID_ROOT, LIKELY_INFLECTION, or GARBAGE.

This module defines the classification interface and a stub implementation.
Swap `classify_batch` for a real AI call when ready.

Classification labels:
  VALID_ROOT        — A genuine standalone dictionary entry. Keep as-is.
  LIKELY_INFLECTION — An inflected form of another word (case suffix, verb
                      conjugation, etc.). Should be removed from Words and
                      optionally added as an inflection under its root.
  GARBAGE           — Noise: digits, punctuation fragments, non-Bangla text,
                      or otherwise meaningless entries. Delete.

Plugging in real AI
-------------------
Replace the body of `_call_ai(batch)` with a Gemini (or other) API call.
The function must return a list of ClassificationResult dicts in the same
order as the input batch. The prompt criteria below define what to look for.

Prompt criteria (for reference when writing the AI prompt):
  1. Is the word a recognisable standalone Bangla lemma (root form)?
  2. Does it carry a case suffix (-এর, -কে, -তে, -থেকে, -র, -য়, -তে etc.)?
  3. Is it a verb conjugation (1st/2nd/3rd person, tense inflections)?
  4. Is it a plural suffix form (-রা, -গুলো, -দের etc.) of an existing word?
  5. Does it contain only non-Bangla characters / digits / punctuation?
  6. Is it a fragment or partial word that has no independent meaning?
"""

from __future__ import annotations

from typing import TypedDict


# ── Types ─────────────────────────────────────────────────────────────────────

class WordInput(TypedDict):
    id: str
    spelling: str


class ClassificationResult(TypedDict):
    id: str
    spelling: str
    classification: str   # VALID_ROOT | LIKELY_INFLECTION | GARBAGE
    reason: str


# Classification label constants
VALID_ROOT = "VALID_ROOT"
LIKELY_INFLECTION = "LIKELY_INFLECTION"
GARBAGE = "GARBAGE"

VALID_LABELS = {VALID_ROOT, LIKELY_INFLECTION, GARBAGE}


# ── Public interface ───────────────────────────────────────────────────────────

def classify_batch(words: list[WordInput]) -> list[ClassificationResult]:
    """
    Classify a batch of words.

    Args:
        words: List of {id, spelling} dicts.

    Returns:
        List of ClassificationResult dicts in the same order as input.
        Each result has: id, spelling, classification, reason.
    """
    if not words:
        return []
    return _call_ai(words)


# ── Implementation (stub — replace with real AI call) ─────────────────────────

def _call_ai(words: list[WordInput]) -> list[ClassificationResult]:
    """
    AI classification stub.

    TODO: Replace this stub with a real Gemini API call.

    Example Gemini call structure to implement here:
        import google.generativeai as genai
        from src.config import settings

        genai.configure(api_key=settings.GEMINI_API_KEY)
        model = genai.GenerativeModel("models/gemini-2.5-flash-lite")
        response = model.generate_content(
            _build_prompt(words),
            generation_config=genai.GenerationConfig(
                temperature=0.1,
                response_mime_type="application/json",
            ),
        )
        return _parse_response(response.text, words)

    The prompt should instruct the model to return a JSON array:
    [
      {"id": "...", "classification": "VALID_ROOT|LIKELY_INFLECTION|GARBAGE", "reason": "..."},
      ...
    ]
    One entry per input word, in the same order.
    """
    return [
        ClassificationResult(
            id=w["id"],
            spelling=w["spelling"],
            classification=VALID_ROOT,
            reason="stub — AI not yet plugged in",
        )
        for w in words
    ]


def _build_prompt(words: list[WordInput]) -> str:
    """
    Build the classification prompt for the AI model.
    Called by the real AI implementation (not the stub).
    """
    word_list = "\n".join(
        f'{i + 1}. id={w["id"]} spelling={w["spelling"]}'
        for i, w in enumerate(words)
    )
    return f"""You are a Bangla linguistics expert. Classify each of the following
Bangla words as exactly one of: VALID_ROOT, LIKELY_INFLECTION, or GARBAGE.

Criteria:
- VALID_ROOT: a genuine standalone lemma with independent meaning in a dictionary
- LIKELY_INFLECTION: a case-marked, conjugated, or suffix-derived form of another word
  (e.g. endings like -এর, -কে, -তে, -থেকে, -রা, -গুলো, or verb tense/person suffixes)
- GARBAGE: non-Bangla text, digit strings, punctuation fragments, or meaningless entries

Words to classify:
{word_list}

Return a JSON array with one object per word in the same order:
[
  {{"id": "<id>", "classification": "<label>", "reason": "<one sentence>"}},
  ...
]
"""
