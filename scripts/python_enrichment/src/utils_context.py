import google.generativeai as genai
import pandas as pd
from config import settings
from google.generativeai import caching
from googleapiclient.discovery import build
from logger import logger
from openai import OpenAI
import json

genai.configure(
    api_key=settings.GEMINI_API_KEY,
)

# if the current word has multipleple words to meaning together, seperate them into two records.
# input will always be a single word's reference.
# in future, we can add:
# pronunciation
# etymology
# usage_notes
# word_forms
# idiomatic_expressions
# transliteration
# derived_words
# etc.

WORD_MEANING_ENTRIES_PROMT = """
You are a helpful multilingual expert in Bangla language, etymology, dictionary
and related fields. Your task is to help me refine, populate, enrich and add meaning
entries created from a Bangla dictionary. Each meaning entry was initially created
manually, focusing on a single meaning of a word. The goal is to extract and enrich
word-meanings collection to a more comprehensive collection with improved attributes
like example sentences, synonyms, antonyms, etc.

Given a "reference" field containing the original source text from a dictionary, and
a list (can be empty) of word-meaning entries (JSON objects), your goal is to refine,
populate, enrich and create word-meaning entries based on the following guidelines:

* **word:** Ensure the parsed value for the word field is correct. IF the root word
appears to be parsed incorrectly, correct it based on your best educated judgement.
For example, if checking with the reference, and it seems a word should be fixed or 
should be two seperate words, then create seperate meaning entries for each of thoese
words (Here, I am just giving an example, it might be the case that some two words are
actually one word). Usually each of the different meaning are coded with <b>1</b>,
<b>2</b>, <b>3</b> etc. Along with that, sometimes a derived word from the root word
is suggested with <b>postfix-in-bangla</b> in the reference string. Create word-meaning
entries for the derived word as well with its different possible meanings. If you can
think of some new derived word that are not suggested on the reference string, feel 
free to create word-meaning entries for that derivation. For the number of word-meaning
entries for a word, use guidance from below.

* **meaning:** Populate, or enrich the meaning description for a particular meaning of
a word/derived word with emphasis on accuracy and completeness. If the meaning field
is incorrect or contain multiple different meanings, then take hint from the "reference"
field and create all possible distinct meaning entries for the word or derived word. 
If the meaning value is empty, then create as many meaning entries as possible for the
candidate word/derived word. If you think there are additional possible meanings, that
are not included in the reference or in the entries, take your time, think and create
distict meaning entries for them. Additionally, combine mutiple meaning entries to one
if they convery almost entirely and exactly the same meaning for different contexts. 
Make sure if you combine to one, you set the additional attributes appropiately incorporating
the merged meanings. Ideally, the meaning description string should NOT contain the
word itself, since the reader is looking up the word without knowing the meaning of the
word.

* **example_sentence:**  For each meaning entry, the example sentence should contain the
the corresponding root or derived word (or a slight variation if that sounds natural or
 is necessary). The word in the sentence should convey the corresponding meaning of the
word. The sentence should create some context for the meaning to have an impact on the
reader. But do not make it difficult to understand. If possible, use everyday or
relatable contexts so readers can quickly infer the intended meaning.

* **synonyms:** Rank ordered synonyms of the root or derived word corresponding the
meaning. Synonyms should ONLY contain Bangla words made up with Bangla characters.
Single word synonyms are preferred. Multi-word synonyms are ONLY acceptable IF they
 are hiphenated or highly appropriate. It is okay if no synonyms exist.

* **antonyms:**  Should contain list of antonyms, similar guidelines as synonyms

Note:
- Take hint/guide from the reference field (don't update it).
- It may contain multiple meanings, and sometimes multiple derived words. 
- Each meaning entry should contain only one of the meanings of the word/derived word.
- Return the output in a structured JSON array suggested in the example.

The following are examples of input and output:

Input 1:
reference:"ককুভ [ kakubha ]   বি. <b>1</b> মার্গসংগীতের রাগিণীবিশেষ; <b>2</b> অর্জুন গাছ; <b>3</b>  দিক। [সং. ক + √ স্কুভ্ + অ]।"
entries:
[
    {
        "reference": "ককুভ [ kakubha ]   বি. <b>1</b> মার্গসংগীতের রাগিণীবিশেষ; <b>2</b> অর্জুন গাছ; <b>3</b>  দিক। [সং. ক + √ স্কুভ্ + অ]।",
        "word": "ককুভ",
        "meaning": "একধরণের সুর যা সংগীতে ব্যবহার করা হয় ",
        "example_sentence": " ককুভের সঙ্গে কাব্য সংগীতের কোনো মিল নেই।",
        "synonyms": "মার্গসংগীতের রাগিণীবিশেষ",
        "antonyms": "নেই",
        "part_of_speech": "বিশেষ্য"
    },
    {
        "reference": "ককুভ [ kakubha ]   বি. <b>1</b> মার্গসংগীতের রাগিণীবিশেষ; <b>2</b> অর্জুন গাছ; <b>3</b>  দিক। [সং. ক + √ স্কুভ্ + অ]।",
        "word": "ককুভ",
        "meaning": "বহুগুণ সম্পন্ন ঔষুধি গাছ",
        "example_sentence": " মার্চ থেকে জুন মাসের মধ্যে ককুভের ফুল ফোটে।",
        "synonyms": "",
        "antonyms": "নেই",
        "part_of_speech": "বিশেষ্য"
    }
]

Output 1 (must contain both "reference" and "entries" keys):
{
    "reference": "ককুভ [ kakubha ]   বি. <b>1</b> মার্গসংগীতের রাগিণীবিশেষ; <b>2</b> অর্জুন গাছ; <b>3</b>  দিক। [সং. ক + √ স্কুভ্ + অ]।",
    "entries": [
        {
            "word": "ককুভ",
            "meaning": "মার্গ সংগীতের একটি বিশেষ রাগিণী",
            "example_sentence": "ককুভ রাগিণী শ্রোতাদের মনে এক অনন্য শান্তি বয়ে আনে।",
            "synonyms": ["রাগিণী", "রাগিণীবিশেষ"],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য",
        },
        {
            "word": "ককুভ",
            "meaning": "অর্জুন গাছ; একটি ঔষধি গাছ",
            "example_sentence": "অর্জুন গাছ বা ককুভ গাছের ছাল ঔষধ হিসেবে ব্যবহৃত হয়।",
            "synonyms": [
                "অর্জুন",
                "ঔষধি গাছ"
            ],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য"
        },
        {
            "word": "ককুভ",
            "meaning": "দিক",
            "example_sentence": "",
            "synonyms": [],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য"
        }
    ]
}

Input 2:
reference:""অপায়ন [ apā&#x1e8f;ana ]   বি. <b>1</b> পলায়ন; <b>2</b> উপায়। [সং.  অপ + √ ই + অন]।"
entries: [
]

Output 2 (must contain both "reference" and "entries" keys) (no entries provided, and new entries are created):
{
    "reference": "অপায়ন [ apā&#x1e8f;ana ]   বি. <b>1</b> পলায়ন; <b>2</b> উপায়। [সং.  অপ + √ ই + অন]।",
    "entries": [
        {
            "word": "অপায়ন",
            "meaning": "পলায়ন; পালিয়ে যাওয়া",
            "example_sentence": "শত্রুর আক্রমণের মুখে অপায়ন ছাড়া আর কোনো উপায় ছিল না।",
            "synonyms": [
                "পলায়ন"
            ],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য"
        },
        {
            "word": "অপায়ন",
            "meaning": "উপায়; সমাধান",
            "example_sentence": "সমস্যা সমাধানের জন্য নতুন অপায়ন খুঁজে বের করতে হবে।",
            "synonyms": [
                "উপায়",
                "সমাধান"
            ],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য"
        }
    ]
}


Input 3:
reference:"কচু [ kacu ] বি. <b>1</b> মানুষের খাদ্য কন্দবিশেষ; <b>2</b> (অবজ্ঞায়) কিছুই না, ঘোড়ার ডিম (তুমি আমার কচু করবে)। [সং. √ কচ্ + উ, তু. হি. কচচূ]। ̃ <b>কাটা</b> বিণ. অবলীলাক্রমে ও সম্পূর্ণভাবে কেটে ফেলা হয়েছে এমন। ̃ <b>ঘেঁচু</b> বি. আজেবাজে শাকসবজি; অখাদ্য বস্তু। ̃ <b>পোড়া</b> বি. অখাদ্য বস্তু; কিছুই নয়।"
entries:[
    {
        'word': 'কচু',
        'meaning': 'কিছুুই জানেনা এমন এমন কিছু যা কখনই সম্ভব নয়',
        'example_sentence': 'তুমি আমার কচু করবে।',
        'synonyms': 'কিছুই না, ঘোড়ার ডিম',
        'antonyms': 'সবকিছু',
        'part_of_speech': 'বিশেষ্য'
    }
]

Output 3 (must contain both "reference" and "entries" keys):
{
    "reference": "কচু [ kacu ] বি. <b>1</b> মানুষের খাদ্য কন্দবিশেষ; <b>2</b> (অবজ্ঞায়) কিছুই না, ঘোড়ার ডিম (তুমি আমার কচু করবে)। [সং. √ কচ্ + উ, তু. হি. কচচূ]। ̃ <b>কাটা</b> বিণ. অবলীলাক্রমে ও সম্পূর্ণভাবে কেটে ফেলা হয়েছে এমন। ̃ <b>ঘেঁচু</b> বি. আজেবাজে শাকসবজি; অখাদ্য বস্তু। ̃ <b>পোড়া</b> বি. অখাদ্য বস্তু; কিছুই নয়।",
    "entries": [
        {
            "word": "কচু",
            "meaning": "মানুষের খাদ্য কন্দবিশেষ",
            "example_sentence": "আমরা কচু দিয়ে তরকারি রান্না করি।",
            "synonyms": [
                "কন্দ",
                "খাদ্য"
            ],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য"
        },
        {
            "word": "কচু",
            "meaning": "(অবজ্ঞায়) কিছুই না, ঘোড়ার ডিম",
            "example_sentence": "তুমি আমার কচু করবে।",
            "synonyms": [
                "কিছুই না",
                "ঘোড়ার ডিম"
            ],
            "antonyms": [
                "সবকিছু"
            ],
            "part_of_speech": "বিশেষ্য"
        },
        {
            "word": "কচুকাটা",
            "meaning": "অবলীলাক্রমে ও সম্পূর্ণভাবে কেটে ফেলা হয়েছে এমন",
            "example_sentence": "কচুকাটা গাছ রাস্তার ধারে পড়ে আছে।",
            "synonyms": [],
            "antonyms": [],
            "part_of_speech": "বিশেষণ"
        },
        {
            "word": "কচুঘেঁচু",
            "meaning": "আজেবাজে শাকসবজি; অখাদ্য বস্তু",
            "example_sentence": "তিনি কচুঘেঁচু খেতে পছন্দ করেন না।",
            "synonyms": [
                "অখাদ্য"
            ],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য"
        },
        {
            "word": "কচুপোড়া",
            "meaning": "অখাদ্য বস্তু; কিছুই নয়",
            "example_sentence": "সে কচুপোড়া খেয়ে অসুস্থ হয়ে পড়েছে।",
            "synonyms": [
                "অখাদ্য"
            ],
            "antonyms": [],
            "part_of_speech": "বিশেষ্য"
        }
    ]
}

(Make sure the output is a valid JSON object and does not encounteer: json.decoder.JSONDecodeError: Expecting ',' delimiter

Now process the following input and return the improved version of the input data:
"""

def get_enrichment_prompt(reference: str, entries: list[dict]):
    # Convert entries to proper JSON string
    entries_json = json.dumps(entries, ensure_ascii=False)
    return f"""
Input:
reference:"{reference}"
entries:{entries_json}
"""

def generate_with_gemini_no_cache(input_data: str) -> str:
    """
    Make an API call to Google's Gemini AI model without content caching

    Args:
        input_data (str): The input data for the model

    Returns:
        str: The generated response from Gemini or error JSON string
    """
    model = genai.GenerativeModel('models/gemini-1.5-flash-001')
    # model = genai.GenerativeModel('models/gemini-1.5-pro')
    # model = genai.GenerativeModel('models/gemini-2.0-flash-001')

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
    
    try:
        response = model.generate_content(
            contents=[WORD_MEANING_ENTRIES_PROMT, input_data], 
            generation_config=generation_config,
            safety_settings=safety_settings
        )
        
        if not response.candidates or not response.candidates[0].content:
            error_msg = "No valid response received from Gemini"
            logger.error(error_msg)
            return '{"status": "error", "message": "' + error_msg + '"}'
            
        return response.text
        
    except Exception as e:
        error_msg = f"Error generating content with Gemini: {str(e)}"
        logger.error(error_msg)
        return '{"status": "error", "message": "' + error_msg + '"}'
