import json
import sys
from collections import defaultdict
import requests

from logger import logger


sys.stdout.reconfigure(encoding="utf-8")

class ReferenceEntries:
    def __init__(self, filename):
        """Initialize tracker with a filename, loading existing dict if available.
        
        Args:
            filename (str): Path to the file storing the processed items
        """
        self.filename = filename
        self.processed_items = {}  # Changed from set to dict
        
        try:
            with open(filename, encoding='utf-8') as f:
                self.processed_items = json.load(f)  # Now loading as dict
        except FileNotFoundError:
            pass

        self.word_entries = defaultdict(list)
        for reference, entries in self.processed_items.items():
            for entry in entries:
                # logger.info(f"Reference: {reference}, Entry: {entry['word']}")
                self.word_entries[entry['word']].append(entry)
            
    def print_entry_count(self):
        """Print the current number of entries in the processed items."""
        count = 0
        for reference, entries in self.processed_items.items():
            count += len(entries)
        logger.info(f"Total entries: {count}")

        logger.info(f"Word entries: {len(self.word_entries)}")

        entry_count_frequency = defaultdict(int)
        for word, entries in self.word_entries.items():
            entry_count_frequency[len(entries)] += 1
            if len(entries) == 72:
                for entry in entries:
                    logger.info(f"Word: {word}, Entry: {entry}")

        for entry_count, frequency in entry_count_frequency.items():
            logger.info(f"Entry count: {entry_count}, Frequency: {frequency}")

        for word, entries in self.word_entries.items():
            if len(entries) == 0:
                logger.info(f"found word with no entries: {word}")

        # for entry_count, frequency in entry_count_frequency.items():
        #     logger.info(f"Entry count: {entry_count}, Frequency: {frequency}")

def create_or_fetch_word(word):
    """Create a new word or fetch existing one from the API.
    
    Args:
        word (str): The word to create or fetch
        
    Returns:
        int|None: The word ID if successful, None otherwise
    """
    try:
        payload = {
            "spelling": word,
        }

        # logger.info(f"Attempting to create word: '{word}'")
        response = requests.post(
            "http://localhost:9000/api/v1/words",
            json=payload
        )
        
        response.raise_for_status()
        response_data = response.json()
        id = response_data["id"]
        # logger.info(f"Successfully created word id:{id}, response: {response_data}")
        return id

    except requests.exceptions.RequestException as e:
        logger.error(f"Failed to create word '{word}': {str(e)}")
    except Exception as e:
        logger.error(f"Unexpected error while creating word '{word}': {str(e)}")

    # If creation failed, try fetching existing word
    try:
        # logger.info(f"Attempting to fetch word by spelling: '{word}'")
        response = requests.post(
            "http://localhost:9000/api/v1/words/postget",
            json={"spelling": word}
        )
        response.raise_for_status()
        response_data = response.json()
        id = response_data["id"]
        # logger.info(f"Successfully fetched word id:{id}, response: {response_data}")
        return id
    except requests.exceptions.RequestException as e:
        logger.error(f"Failed to fetch word '{word}': {str(e)}")
    except Exception as e:
        logger.error(f"Unexpected error while fetching word '{word}': {str(e)}")
    
    return None

if __name__ == "__main__":
    references = ReferenceEntries("data/completed_references.json")
    references.print_entry_count()
    work_meanings_list = list(references.word_meanings.items())
    work_meanings_list = sorted(work_meanings_list, key=lambda x: len(x[1]), reverse=True)

    # TO_CREATE = 1000
    total_meanings = 0
    for i, (word, meanings) in enumerate(work_meanings_list):
        # if i > TO_CREATE - 1:
        #     break

        word = word.strip()
        id = create_or_fetch_word(word)
        if id is None:
            logger.error(f"Failed to create or fetch word '{word}'")
            continue

        # logger.info(f"Word: '{word}', id: {id}")

        for j, meaning in enumerate(meanings):
            # create meaning using api for the word
            payload = {
                "id": None,
                "text": meaning["meaning"].strip(),
                "partOfSpeech": meaning["part_of_speech"],
                "strength": 0,
                "antonyms": meaning["antonyms"],
                "synonyms": meaning["synonyms"],
                "exampleSentence": meaning["example_sentence"].strip(),
            }

            try:
                response = requests.post(
                    f"http://localhost:9000/api/v1/words/{id}/meanings",
                    json=payload
                )
                response.raise_for_status()
                response_data = response.json()
                # if i > 0:
                    # logger.info(f"Added multiple meanings for word '{word}'")
                # logger.info(f"Successfully added meaning {i+1}: {response_data}")
            except requests.exceptions.RequestException as e:
                logger.error(f"Failed to add meaning {j+1} for word '{word}': {str(e)}. Continuing to next meaning.")
                continue
            except Exception as e:
                logger.error(f"Unexpected error while adding meaning {j+1} for word '{word}': {str(e)}. Continuing to next meaning.")
                continue

        total_meanings += len(meanings)

        if i % 1000 == 0:
            logger.info(f"Processed {i + 1} words, total meanings: {total_meanings}")
