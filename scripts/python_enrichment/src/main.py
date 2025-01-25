from util import generate_with_gemini, get_enrichment_prompt, read_google_sheet,  generate_with_deepseek
from logger import logger
import json


def df_to_json_entries(df):
    """Convert DataFrame rows to list of JSON objects with field mapping."""
    # Define field mappings (old_name: new_name)
    field_mappings = {
        'Reference (Do not cut)': 'reference',
        'শব্দ সমূহ'        : 'word',
        'সহজ উদাহরণ'    : 'meaning',
        'বাক্য রচনা'      : 'example_sentence',
        'সমার্থক শব্দ'      : 'synonyms',
        'বিপরীতার্থক শব্দ'   : 'antonyms',
        'পদ'            : 'part_of_speech',
    }
    
    json_entries = []
    for _, row in df.iterrows():
        # Create new dict with mapped fields
        entry = {}
        for old_field, new_field in field_mappings.items():
            if old_field in row:
                entry[new_field] = row[old_field]
        json_entries.append(entry)
    return json_entries

if __name__ == "__main__":
    # The sheet_key is the long string from your Google Sheets URL 
    sheet_key = "1OUwV-WrVAEMTOVUNzGcrF3MGZGVIzMmj87bbLyicnvM"
    df = read_google_sheet(sheet_key)
    
    # Convert DataFrame to JSON entries
    json_entries = df_to_json_entries(df)

    # # Example usage
    # sample_data = {
    #     "word": "লাফ",
    #     "meaning": "উচ্চতায় বা দূরত্বে উপরে ওঠা বা সামনে যাওয়া।",
    #     "example_sentence": "বিড়ালটি টেবিল থেকে লাফ দিল।",
    #     "synonyms": ["উঠান", "ঝাঁপ", "উঁচুতে ওঠা"],
    #     "antonyms": ["পড়ে যাওয়া", "থেমে যাওয়া"]
    # }
    
    for entry in json_entries[:20]:
       logger.info(f"Provided entry: {json.dumps(entry, ensure_ascii=False, indent=2)}")
       prompt = get_enrichment_prompt(entry)
       logger.info(f"Prompt: {prompt}")
    #    response = generate_with_gemini(prompt)
    #    logger.info(f"Gemini response: {response}")
    #    response = generate_with_deepseek(prompt)
    #    logger.info(f"Deepseek response: {response}")
