import json
import sys
from collections import defaultdict

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



if __name__ == "__main__":
    references = ReferenceEntries("completed_references.json")
    references.print_entry_count()

