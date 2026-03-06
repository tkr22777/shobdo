import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

import google.generativeai as genai
from config import settings
from logger import logger

genai.configure(api_key=settings.GEMINI_API_KEY)

MISSING_WORD_PROMPT = """You are a Bangla language expert and dictionary editor.

Given a single Bangla word or phrase, decide ONE of three things:

A) It deserves its own dictionary entry → generate all distinct meanings.
B) It is a grammatical inflection of a simpler root word → return the root.
C) It is truly invalid (garbled, random characters) → return the reason.

━━━ RULE B — INFLECTION DETECTION ━━━

A word is an INFLECTION (choose B) when:
- It is a surface-level grammatical form of a base/root word, obtained by adding
  a Bangla case suffix, number suffix, tense marker, or similar grammatical ending.
- The root word is a real, well-formed Bangla word in its own right.
- The inflected form adds NO new lexical meaning beyond what the root already covers.

Common Bangla inflection patterns:
  Case   : -র / -এর / -দের (genitive)  →  আলোচনার → root: আলোচনা
           -য় / -তে / -এ (locative)    →  আলোচনায় → root: আলোচনা
           -কে (objective)              →  বইকে    → root: বই
           -থেকে (ablative)             →  বাড়িথেকে → root: বাড়ি
  Number : -রা / -গুলো / -গুলি         →  মানুষেরা → root: মানুষ
  Determ.: -টি / -টা                   →  বইটি    → root: বই
  Verb   : tense endings (-ছিল, -ছিলাম, -েছে, -বে, -ল, -লাম, -লেন)
           →  বলেছিলাম → root: বলা

Do NOT choose B if:
- The form has an independent, distinct lexical meaning from the root.
- It is a fixed expression, idiom, compound word, or adverb formed from a root.
- The "root" you'd suggest is itself not a real Bangla word.

━━━ RULE C — INVALID ━━━

INVALID only when the input is:
- Random characters with no Bengali morphological basis (e.g. "কখজঞতথদ")
- Clearly garbled, corrupted, or a transcription error
- Has absolutely no conceptual existence as a Bangla word or expression

━━━ OUTPUT FORMAT ━━━

If VALID (A) — return exactly this JSON structure:
{
  "valid": true,
  "entries": [
    {
      "word": "<the word exactly as given>",
      "meaning": "<one distinct meaning; do not include the word itself>",
      "example_sentence": "<a natural Bangla sentence using the word in this meaning>",
      "synonyms": ["<Bangla word>", "..."],
      "antonyms": ["<Bangla word>", "..."],
      "part_of_speech": "<বিশেষ্য | বিশেষণ | ক্রিয়া | ক্রিয়াবিশেষণ | অব্যয় | সর্বনাম | ...>"
    }
  ]
}

If INFLECTION (B) — return exactly this JSON structure:
{
  "valid": false,
  "inflection": true,
  "root": "<the base/root word in its simplest form>",
  "reason": "<one short phrase: e.g. 'locative suffix -য়', 'genitive suffix -র'>"
}

If NOT VALID (C) — return exactly this JSON structure:
{
  "valid": false,
  "inflection": false,
  "reason": "<one short sentence explaining why>"
}

Rules for entries:
- One entry per distinct meaning. Generate as many as the word warrants.
- synonyms and antonyms must be Bangla words only — no English, no Roman letters.
- synonyms/antonyms may be empty arrays [] if none exist.
- Do not use the word in its own meaning field.

━━━ EXAMPLE SENTENCE RULES ━━━

RULE 1 — EXACT SPELLING:
Use the word with its exact spelling as given. Do not drop suffixes, strip endings,
remove diacritics (like hasanta ্), or revert to a base/root form if the given word
is already inflected. If the exact form feels awkward, restructure the sentence.

  Word: আয়ুর
  ✗ BAD:  "মানুষের আয়ু ক্ষণস্থায়ী।"
           (wrong — uses আয়ু, not আয়ুর)
  ✓ GOOD: "আয়ুর শেষ প্রান্তে এসে তিনি বুঝলেন কোনটা আসল ছিল, কোনটা ছিল না।"
           (correct — আয়ুর appears exactly as given)

  Word: দাপটে
  ✗ BAD:  "সে অনেক দাপট দেখাল।"
           (wrong — uses দাপট, not দাপটে)
  ✓ GOOD: "সে এত দাপটে হুকুম করল যে ঘরের সবাই নিঃশব্দে মাথা নামিয়ে নিল।"
           (correct — দাপটে appears exactly as given)

RULE 2 — WORD MUST CARRY REAL WEIGHT:
If you removed the word from the sentence and replaced it with a vague placeholder,
the sentence should fall apart or lose its specific meaning. The word must be the
load-bearing element of the sentence, not decoration.

  Word: যাচনা
  ✗ BAD:  "যাচনা করা উচিত নয়।"
           (hollow — any word could replace যাচনা, sentence conveys nothing specific)
  ✓ GOOD: "বারবার যাচনা করেও সে তার ন্যায্য পাওনা পেল না — শেষমেশ নিজেই লড়াই করতে নামল।"
           (strong — যাচনা is the pivot of the whole scene)

RULE 3 — NO PASSIVE FILLER CONSTRUCTIONS:
Avoid passive templates like "X ব্যবহার করা হয়", "X শুনতে ভালো লাগে",
"X দেখা যায়" where the word slots in mechanically with no context.

  Word: স্প্রে
  ✗ BAD:  "মশা তাড়ানোর জন্য স্প্রে ব্যবহার করা হয়।"
           (passive, generic — tells the reader nothing beyond a dictionary label)
  ✓ GOOD: "ছেলেটি বাগানের গাছে রোজ সকালে কীটনাশক স্প্রে করে তবেই স্কুলে যায়।"
           (active, specific — the word sits inside a real scene)

RULE 4 — CLARITY OVER POETRY:
A plain, clear sentence is better than a decorative one. Literary references or
proverbs are welcome when they fit naturally, but never force a poetic tone.
The sentence should make the meaning clear from context alone.

━━━ FULL EXAMPLES ━━━

Input: word: "গৃহিণী"
Output:
{
  "valid": true,
  "entries": [
    {
      "word": "গৃহিণী",
      "meaning": "গৃহের কর্ত্রী; যিনি সংসার পরিচালনা করেন",
      "example_sentence": "গৃহিণী সকালে উঠে পরিবারের জন্য রান্না করলেন এবং বাচ্চাদের স্কুলের জন্য প্রস্তুত করলেন।",
      "synonyms": ["গৃহকর্ত্রী", "সংসারিণী", "গেরস্তবধূ"],
      "antonyms": ["গৃহকর্তা"],
      "part_of_speech": "বিশেষ্য"
    }
  ]
}

Input: word: "তবুও"
Output:
{
  "valid": true,
  "entries": [
    {
      "word": "তবুও",
      "meaning": "তথাপি; প্রতিকূল পরিস্থিতি সত্ত্বেও",
      "example_sentence": "হাজার ঝড় এসেছে, হাজার রাত কেটেছে অন্ধকারে — তবুও সে মাথা নোয়ায়নি।",
      "synonyms": ["তথাপি", "তবু", "তাও"],
      "antonyms": [],
      "part_of_speech": "অব্যয়"
    }
  ]
}

Input: word: "মাটি"
Output:
{
  "valid": true,
  "entries": [
    {
      "word": "মাটি",
      "meaning": "ভূপৃষ্ঠের কঠিন স্তর; মৃত্তিকা",
      "example_sentence": "বৃষ্টির পর ভেজা মাটির গন্ধ নাকে এলে মনে হয় পৃথিবীটা নতুন হয়ে জন্ম নিল।",
      "synonyms": ["মৃত্তিকা", "ধূলি", "ভূমি"],
      "antonyms": ["আকাশ"],
      "part_of_speech": "বিশেষ্য"
    },
    {
      "word": "মাটি",
      "meaning": "জন্মভূমি; দেশের প্রতি আত্মিক টান",
      "example_sentence": "বিদেশে দশ বছর কাটিয়েও এই মাটির টান তাকে শেষ পর্যন্ত ঘরে ফিরিয়ে আনল।",
      "synonyms": ["জন্মভূমি", "দেশ", "স্বদেশ"],
      "antonyms": ["বিদেশ"],
      "part_of_speech": "বিশেষ্য"
    },
    {
      "word": "মাটি",
      "meaning": "(আলঙ্কারিক) নষ্ট বা বিফল হওয়া",
      "example_sentence": "বছরের পর বছর যা গড়েছিল, একটা ভুল সিদ্ধান্তে সব মাটি হয়ে গেল।",
      "synonyms": ["বরবাদ", "নষ্ট", "বিফল"],
      "antonyms": ["সফল", "সার্থক"],
      "part_of_speech": "বিশেষণ"
    }
  ]
}

Input: word: "আলোচনায়"
Output:
{
  "valid": false,
  "inflection": true,
  "root": "আলোচনা",
  "reason": "locative suffix -য়"
}

Input: word: "ছাত্রছাত্রীদের"
Output:
{
  "valid": false,
  "inflection": true,
  "root": "ছাত্রছাত্রী",
  "reason": "genitive plural suffix -দের"
}

Input: word: "কখজঞতথদ"
Output:
{
  "valid": false,
  "inflection": false,
  "reason": "এটি এলোমেলো বর্ণের সমষ্টি; বাংলায় কোনো পরিচিত শব্দ বা প্রকাশভঙ্গি হিসেবে এর অস্তিত্ব নেই।"
}

━━━ NOW PROCESS ━━━

"""


GEMINI_MODEL = "models/gemini-2.5-flash-lite"


def generate_missing_word(word: str) -> str:
    """Call Gemini to generate a dictionary entry for a single Bangla word.

    Returns:
        JSON string: {"valid": true, "entries": [...]}
                  or {"valid": false, "inflection": true, "root": "...", "reason": "..."}
                  or {"valid": false, "inflection": false, "reason": "..."}
                  or {"status": "error", "message": "..."} on API/parse failure.
    """
    model = genai.GenerativeModel(GEMINI_MODEL)

    generation_config = genai.types.GenerationConfig(
        temperature=0.15,
        response_mime_type="application/json",
    )

    safety_settings = {
        "HARM_CATEGORY_HARASSMENT": "BLOCK_NONE",
        "HARM_CATEGORY_HATE_SPEECH": "BLOCK_NONE",
        "HARM_CATEGORY_SEXUALLY_EXPLICIT": "BLOCK_NONE",
        "HARM_CATEGORY_DANGEROUS_CONTENT": "BLOCK_NONE",
    }

    user_prompt = f'word: "{word}"'

    try:
        response = model.generate_content(
            contents=[MISSING_WORD_PROMPT, user_prompt],
            generation_config=generation_config,
            safety_settings=safety_settings,
        )

        if not response.candidates or not response.candidates[0].content:
            logger.error(f"Empty Gemini response for word: '{word}'")
            return '{"status": "error", "message": "empty response"}'

        return response.text

    except Exception as e:
        logger.error(f"Gemini API error for word '{word}': {e}")
        return '{"status": "error", "message": "api error"}'
