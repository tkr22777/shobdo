import datetime

import google.generativeai as genai
import pandas as pd
from tenacity import retry, retry_if_exception_type, stop_after_attempt, wait_fixed
from config import settings
from google.generativeai import caching
from googleapiclient.discovery import build
from logger import logger
from openai import OpenAI

import utils_context


def generate_with_gemini(input_data: str) -> str:
    """
    Make an API call to Google's Gemini AI model with content caching support

    Args:
        prompt (str): The input prompt for the model

    Returns:
        str: The generated response from Gemini
    """
    cache_ttl = datetime.timedelta(minutes=1)
    model='models/gemini-1.5-flash-001'

    cache = caching.CachedContent.create(
        model=model,
        display_name='empty_cache',
        system_instruction="You are a helpful assistant.",
        contents=[],
        ttl=cache_ttl
    )

    model = genai.GenerativeModel.from_cached_content(cached_content=cache)

    generation_config = genai.types.GenerationConfig(
        temperature=0.15, response_mime_type="application/json"
    )
    response = model.generate_content(input_data, generation_config=generation_config)
    return response.text

@retry(
    stop=stop_after_attempt(1),  # Initial attempt + retries combined
    wait=wait_fixed(0.2),  # Wait 0.2 second between retries
    retry=retry_if_exception_type(Exception),
)
def generate_with_deepseek(prompt: str) -> str:
    client = OpenAI(
        api_key=settings.DEEPSEEK_API_KEY,
        base_url="https://api.deepseek.com",
    )

    response = client.chat.completions.create(
        model="deepseek-chat",
        messages=[
            {"role": "system", "content": utils_context.WORD_MEANING_ENTRIES_PROMT},
            {"role": "user", "content": f"{prompt}"},
        ],
        stream=False,
    )
    return response.choices[0].message.content

# sheet_keys = [
#     "1OUwV-WrVAEMTOVUNzGcrF3MGZGVIzMmj87bbLyicnvM",
#     "1IIM9VzMtzQSzlhHzEeE2J49Tf1JYio3fGPg7TfFujZE",
#     "10xTWxiN3AMstD5RKOlatlo01rjyFptUcPn2Ae2LAA7w",
#     "1J7YMhhwyWx1LsDKWIIdneR8affQsEdsUGDg4y-pJ7Q8",
#     "1nPyY8BQ80wr96B1BICd6-VF_YNl56OcpkaT2hUgVGu8",
#     "1-HMmjUdANd_LHC76w5-HFp9fPJF2yZrDiIAqbX8a8g4",
#     "13dzFrXSCe3g9BsId74hrBADguExFjR6mFYt-bwEVS08",
#     "1sRE2MCT9r_ybcMr9mSD1EAtoqBpwsKmUMwB0qZFTwe0",
#     "1SiwvhfmuoF2FkW8M-fuaZviAVvIZafrKWmA3zBsdY2E",
#     "1er2ADe7qnl5rwNAxjacPKlwyxzzf_6EDg86f93VA3OQ",
#     "1YIonbDDDgDaHJh1z9wMIaGobl98uNftGuDMdSKNkjVQ",
#     "1Opatwx1_YYoOT9sS02DhiL1ZK0L_Cv_lFwkkpXNVKT0",
#     "1C1TnPRXs_8h4jeuHOLQeRG6spKyJs8JOOsjcyjRWnJU",
#     "1nbchyI4aBsGPu2DP03_-mtrvEYEK8ubUoOJFHrJzOco",
#     "1T8XPfG5Is97YBQlHzREselV2jJlGCZtuxZkxCkwHj_g",
#     "1kqIbmFpk4_D5kr4aWp6DR3S3PbcaLBeHwwLDcVEb50k",
#     "1H0LGuR-wfImW3eFvmcW_sn1ofdRw4VdBr13N9uCc7sg",
#     "16wK6D5BOwQ1RTq9rPoTGqk_791jDrL2JW-bciqnL36w",
#     "1_6OP2UUrppYsUDqKzi9a03ZEs1gzArhA03s0X5r5yr8",
#     "1m7KFQWax9i4l413ROGmetGM72pQUnlYKZ9QSSyqMWng"
# ]

# sheet_keys = [
#     "1laB-_YfouKBmgcK1tyyECQHHUYm3uVe9adE2CH_Dr6E"
# ]

def read_google_sheet(sheet_key: str, range_name: str = "Data") -> pd.DataFrame:
    """
    Read data from a Google Sheet and return as a pandas DataFrame using API key

    Args:
        sheet_key (str): The key from the Google Sheet URL
        range_name (str): Name of the range to read (default: "Sheet1")

    Returns:
        pd.DataFrame: DataFrame containing the sheet data
    """
    service = build("sheets", "v4", developerKey=settings.GOOGLE_SHEETS_API_KEY)

    sheet = service.spreadsheets()
    result = sheet.values().get(spreadsheetId=sheet_key, range=range_name).execute()

    values = result.get("values", [])

    if not values:
        logger.warning("No data found in sheet")
        return pd.DataFrame()

    headers = values[0]
    
    if not headers:
        logger.error("Headers row is empty")
        return pd.DataFrame()
        
    data = values[1:]

    normalized_data = []
    num_cols = len(headers)
    for row in data:
        normalized_row = row + [None] * (num_cols - len(row))
        normalized_data.append(normalized_row)

    try:
        df = pd.DataFrame(normalized_data, columns=headers)
        return df
    except ValueError as e:
        logger.error(f"DataFrame creation failed: {e}")
        logger.error(f"Headers length: {len(headers)}, Data columns: {len(normalized_data[0]) if normalized_data else 0}")
        raise


def df_to_json_entries(df):
    """Convert DataFrame rows to list of JSON objects with field mapping."""
    # Define field mappings (old_name: new_name)
    field_mappings = {
        "Reference (Do not cut)": "reference",
        "শব্দ সমূহ": "word",
        "সহজ উদাহরণ": "meaning",
        "বাক্য রচনা": "example_sentence",
        "সমার্থক শব্দ": "synonyms",
        "বিপরীতার্থক শব্দ": "antonyms",
        "পদ": "part_of_speech",
    }

    json_entries = []
    found_fields = set()  # Track all field names found in the rows
    
    for _, row in df.iterrows():
        found_fields.update(row.index)
        
        # Check for missing required fields
        if "Reference (Do not cut)" not in row:
            logger.error(f"Row missing reference field: {row.to_dict()}")
            continue

        # if "শব্দ সমূহ" not in row:
        #     logger.error(f"Row missing word field: {row.to_dict()}")
        #     continue
            
        # if "সহজ উদাহরণ" not in row:
        #     logger.error(f"Row missing meaning field: {row.to_dict()}")

        # Create new dict with mapped fields
        entry = {}
        for old_field, new_field in field_mappings.items():
            if old_field in row:
                entry[new_field] = row[old_field]
        json_entries.append(entry)
    return json_entries, found_fields
