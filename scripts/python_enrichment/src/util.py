import google.generativeai as genai
from config import settings
from logger import logger
from openai import OpenAI

def generate_with_gemini(prompt: str) -> str:
    """
    Make an API call to Google's Gemini AI model
    
    Args:
        prompt (str): The input prompt for the model
        
    Returns:
        str: The generated response from Gemini
    """
    # logger.info(f"Generating response for prompt: {prompt}")
    genai.configure(
        api_key=settings.GEMINI_API_KEY,
    )
    model = genai.GenerativeModel('gemini-1.5-flash-8b')
    # model = genai.GenerativeModel('gemini-1.5-pro')

    generation_config = genai.types.GenerationConfig(
        temperature=0.25,
        response_mime_type="application/json"
    )
    response = model.generate_content(prompt, generation_config=generation_config)
    # logger.info("Successfully generated response")
    return response.text

def generate_with_deepseek(prompt: str) -> str:
    client = OpenAI(api_key="sk-04492f533946493a915c766fd8c7cf0a", base_url="https://api.deepseek.com")

    response = client.chat.completions.create(
        model="deepseek-chat",
        messages=[
            {"role": "system", "content": "You are a helpful assistant"},
            {"role": "user", "content": f"{prompt}"},
        ],
        stream=False
    )
    # logger.info(response.choices[0].message.content)
    return response.choices[0].message.content

# in future, we can add:
# pronunciation
# etymology
# usage_notes
# word_forms
# idiomatic_expressions
# transliteration
# derived_words
# etc.

def get_enrichment_prompt(input_data):
    return f"""
You are a helpful multilingual expert in Bangla language, etymology and related fields. 
You are helping me with the data parsing, cleaning, and enrichment of JSON records
from a dictionary provided in Bangla. It has fields that correspond to their values.
E.g. the `meaning` field corresponds to one of the meanings of the root word,
and `synonyms` and `antonyms` correspond to synonyms/antonyms for the word (with respect
to that meaning). Althought, some values for a field may not be entirely correct or
properly populated.

Your task:
- Refine the values of the fields if they exist. Improve the `meaning`, 
  `example_sentence`, `synonyms`, and `antonyms` wherever necessary.
- Fill in missing fields (e.g., synonyms or antonyms) if you are confident.
  For instance, you can add synonyms/antonyms that do not currently exist
  but should logically be present.
- Ensure the example sentence contains the exact root word. If that is difficult to
  achieve, you can use a slight variation of the root word.
- Ensure that synonyms and antonyms are **preferably single words**. 
  If no suitable single-word options are available, only include very appropriate
  multiword synonyms/antonyms. It is okay to not have synonyms/antonyms for a word.
- Take hint from the reference field, but do not add it on the output,it was collected
  from a dictionary. It can guide in fixing/improving other fields. It may contain
  multiple meanings. Only one of the meaning is used under the `meaning`
  field and the rest are ignored as they are processed seperately. if none of the
  meaning from the refernce field is parsed on the meaning field, then you
  can create multiple JSON responses (each as an object in an array) for different 
  meanings.
- In some cases, the word and the meaning might not be parsed, then use your knowledge
  to populate the fields. If meaning is not properly parsed or created, then you can
  create the meaning from the reference field. In that case, if you think multiple meanings
  are possible, then create each corresponding to a single meaning.
- Return the output in a structured JSON array format

Here is an example:

Input:
{{
  "reference": "দৌড় [ dauḍ় ]   বি. <b>1</b> ছুট, ধাবন, বেগে যাওয়া (দৌড় প্রতিযোগিতা,   দৌড় দাও, নইলে গাড়ি পাবে না); <b>2</b> বেগে পলায়ন  (পুলিশ দেখেই দৌড় দিল); <b>3</b> (ব্যঙ্গে) ক্ষমতা (দেখব  তোমার দৌড় কতদূর)। [< সং. √ দ্রু + বাং. অ-তু.  হি. মৈ. দৌড়]। ̃ <b>ঝাঁপ</b> বি. <b>1</b> দৌড় ও লাফ; <b>2</b>  দাপাদাপি; <b>3</b> ব্যস্ততার সঙ্গে ছোটাছুটি (আমাদের আর  দৌড়ঝাঁপ করার বয়স নেই)। <b>দৌড় দেওয়া, দৌড় মারা</b>  ক্রি. বি.  <b>1</b> ছুটে যাওয়া; <b>2</b> বেগে পলায়ন করা।  <b>দৌড়া-দৌড়ি</b> বি. ক্রমাগত দৌড়, ছোটাছুটি। <b>দৌড়ানো</b>  ক্রি. বি. দৌড় দেওয়া, ছোটা। ☐ বিণ. উক্ত অর্থে।"
  "word": "দৌড়",
  "meaning": "দ্রুত পদচারণা করে অগ্রসর হওয়া।",
  "example_sentence": "সে প্রতিদিন সকালে পার্কে দৌড়ায়।",
  "synonyms": "ছোটা, ছুটে চলা",
  "antonyms": "থেমে যাওয়া, স্থির থাকা",
  "part_of_speech": "ক্রিয়া"
}}

Output:
[
  {{
    "word": "দৌড়",
    "meaning": "দ্রুত পায়ে চলার প্রক্রিয়া, যা প্রায়শই শরীরচর্চা বা প্রতিযোগিতার জন্য করা হয়।",
    "example_sentence": "সে সকালে পার্কে দৌড় দিয়ে শরীরচর্চা করে।",
    "synonyms": ["ছোটা", "দ্রুতগমন", "অগ্রসরতা"],
    "antonyms": ["স্থিরতা", "বিরতি", "থেমে যাওয়া"],
    "part_of_speech": "ক্রিয়া"
  }},
]

Now process the following input and return the improved version of the input data:

Input:
{input_data}
"""

import pandas as pd
from googleapiclient.discovery import build
from config import settings

def read_google_sheet(sheet_key: str, range_name: str = "Data") -> pd.DataFrame:
    """
    Read data from a Google Sheet and return as a pandas DataFrame using API key
    
    Args:
        sheet_key (str): The key from the Google Sheet URL
        range_name (str): Name of the range to read (default: "Sheet1")
        
    Returns:
        pd.DataFrame: DataFrame containing the sheet data
    """
    service = build('sheets', 'v4', developerKey=settings.GOOGLE_SHEETS_API_KEY)
    
    # Call the Sheets API
    sheet = service.spreadsheets()
    result = sheet.values().get(
        spreadsheetId=sheet_key,
        range=range_name
    ).execute()
    
    values = result.get('values', [])
    
    if not values:
        logger.warning('No data found in sheet')
        return pd.DataFrame()
        
    # First row as headers
    headers = values[0]
    data = values[1:]
    
    # Convert to pandas DataFrame
    df = pd.DataFrame(data, columns=headers)
    return df
