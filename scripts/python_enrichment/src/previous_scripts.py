from concurrent.futures import ThreadPoolExecutor, as_completed
from openai import OpenAI
from config import settings
from collections import defaultdict
import copy
import json
from logger import logger
from main import ProgressTracker
from tenacity import retry, stop_after_attempt, wait_fixed, retry_if_exception_type
from utils_context import (
    WORD_MEANING_ENTRIES_PROMT,
    get_enrichment_prompt,
)



class ProgressTracker:
    """Tracks progress by maintaining a dict of references and their entries that persists across runs."""
    
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
    
    def add(self, reference: str, entries: list):
        """Add a reference and its entries to the processed dict.
        
        Args:
            reference (str): Reference key
            entries (list): List of entry dictionaries
        """
        self.processed_items[reference] = entries
    
    def save(self):
        """Save the current dict of processed items to file as JSON."""
        with open(self.filename, 'w', encoding='utf-8') as f:
            json.dump(self.processed_items, f, ensure_ascii=False, indent=2)
    
    def print_progress(self):
        """Print the current number of processed items."""
        logger.info(f"Number of processed items: {len(self.processed_items)}")
    
    def print_entry_count(self):
        """Print the current number of entries in the processed items."""
        count = 0
        for reference, entries in self.processed_items.items():
            count += len(entries)
        logger.info(f"Total entries: {count}")
    
    def __contains__(self, item):
        """Check if a reference has been processed."""
        return item in self.processed_items


def process_batch_with_gemini(references_batch: dict) -> dict:
    """Process a batch of references in parallel using Gemini API.
    
    Args:
        references_batch (dict): Dictionary of reference keys to their entries
        
    Returns:
        dict: Dictionary mapping references to their processed entries
    """
    batch_results = {}
    with ThreadPoolExecutor(max_workers=100) as executor:
        # Submit all tasks and store futures with their references
        future_to_ref = {
            executor.submit(process_entries_with_gemini, reference, entries): reference
            # executor.submit(generate_with_deepseek, reference, entries): reference
            for reference, entries in references_batch.items()
        }
        
        # Process completed futures as they finish
        for future in as_completed(future_to_ref):
            reference = future_to_ref[future]
            try:
                parsed_response = future.result()
                if parsed_response is None:
                    continue
                batch_results[reference] = parsed_response["entries"]
            except Exception as e:
                # Check if e is a RetryError by examining if it has the `last_attempt` attribute.
                underlying_error = e
                if hasattr(e, "last_attempt"):
                    underlying_error = e.last_attempt.exception()
    
                if "Error in parsing response" in str(underlying_error):
                    logger.error(
                        f"Skipping reference {reference} due to parsing error: {str(underlying_error)}"
                    )
                    continue
                else:
                    logger.exception(f"Failed to process reference {reference} in batch: {str(e)}")
                    raise
                
    return batch_results

@retry(
    stop=stop_after_attempt(1),  # Initial attempt + retries combined
    wait=wait_fixed(0.2),  # Wait 0.2 second between retries
    retry=retry_if_exception_type(Exception),
)
def process_entries_with_gemini(reference: str, entries: list) -> dict:
    """Process entries using Gemini API and handle the response.
    
    Args:
        reference (str): Reference key
        entries (list): List of entry dictionaries
        
    Returns:
        dict: Processed entries from Gemini
        
    Raises:
        Exception: If response is invalid or missing entries after all retries
    """
    entries_deep_copy = copy.deepcopy(entries)
    for entry in entries_deep_copy:
        del entry["reference"]
    
    prompt = get_enrichment_prompt(reference, entries_deep_copy)
    response = generate_with_gemini_no_cache(prompt)

    try:
        parsed_response = json.loads(response)
        response = json.dumps(parsed_response, ensure_ascii=False, indent=2)
    except json.JSONDecodeError:
        logger.exception(f"Error in parsing response for {reference}")
        raise Exception(f"Error in parsing response for {reference}")

    if "entries" not in parsed_response:
        logger.exception(f"Invalid response for {reference}")
        return None
        # raise Exception(f"Invalid response for {reference}")

    return parsed_response

@retry(
    stop=stop_after_attempt(1),  # Initial attempt + retries combined
    wait=wait_fixed(0.2),  # Wait 0.2 second between retries
    retry=retry_if_exception_type(Exception),
)
def generate_with_deepseek(reference: str, entries: list) -> dict:
    entries_deep_copy = copy.deepcopy(entries)
    for entry in entries_deep_copy:
        del entry["reference"]
    
    prompt = get_enrichment_prompt(reference, entries_deep_copy)
    
    client = OpenAI(
        api_key=settings.DEEPSEEK_API_KEY,
        base_url="https://api.deepseek.com",
    )

    response = client.chat.completions.create(
        model="deepseek-chat",
        messages=[
            {"role": "system", "content": WORD_MEANING_ENTRIES_PROMT},
            {"role": "user", "content": f"{prompt}"},
        ],
        stream=False,
    )
    
    try:
        parsed_response = json.loads(response.choices[0].message.content)
        response = json.dumps(parsed_response, ensure_ascii=False, indent=2)
    except json.JSONDecodeError:
        logger.exception(f"Error in parsing response for {reference}")
        raise Exception(f"Error in parsing response for {reference}")

    if "entries" not in parsed_response:
        logger.exception(f"Invalid response for {reference}")
        return None
        # raise Exception(f"Invalid response for {reference}")

    return parsed_response

def group_entries_by_reference(entries):
    """Group entries by their reference field and log any entries with missing references.
    
    Args:
        entries (list): List of dictionary entries containing reference keys
        
    Returns:
        defaultdict: Dictionary mapping references to lists of entries
    """
    entries_by_reference = defaultdict(list)
    for entry in entries:
        if entry["reference"] is None:
            logger.error(f"Reference is None for {entry}")
            continue

        entries_by_reference[entry["reference"]].append(entry)

    logger.info(f"Total unique references: {len(entries_by_reference)}")
    return entries_by_reference

def analyze_entry_sizes(entries_by_reference):
    """Analyze the frequency of different entry sizes in the dataset.
    
    Args:
        entries_by_reference (dict): Dictionary mapping references to lists of entries
        
    Returns:
        defaultdict: Dictionary mapping entry sizes to lists of entries
    """
    entry_size_frequency = defaultdict(list)
    for reference, entries in entries_by_reference.items():
        entry_size_frequency[len(entries)].append(entries)

    for size, entries in sorted(entry_size_frequency.items(), key=lambda x: x[0]):
        logger.info(f"Size: {size}, Frequency: {len(entries)}")
        
    return entry_size_frequency


def process_references():

    progress = ProgressTracker("completed_references.json")
    # progress.print_progress()
    # progress.print_entry_count()

    # Read back and verify
    initially_processed_file = "initially_processed_file.json"
    entires_requiring_enrichment = "entires_requiring_enrichment.json" 

    # Load and count entries from processed files
    with open(initially_processed_file, encoding="utf-8") as f:
        processed_entries = json.load(f)
    logger.info(f"Total entries in {initially_processed_file}: {len(processed_entries)}")

    with open(entires_requiring_enrichment, encoding="utf-8") as f:
        entries_require_processing = json.load(f)
    logger.info(f"Total entries in {entires_requiring_enrichment}: {len(entries_require_processing)}")

    all_entries = processed_entries + entries_require_processing

    logger.info(f"Total entries: {len(all_entries)}")

    entries_by_reference = group_entries_by_reference(all_entries)
    entry_size_frequency = analyze_entry_sizes(entries_by_reference)
    
    # Filter references that need processing
    references_to_process = {
        ref: entries for ref, entries in entries_by_reference.items() 
        if ref not in progress
    }

    logger.info(f"References requiring processing: {len(references_to_process)}")
    
    # Convert dictionary to sorted list of (reference, entries) tuples for deterministic ordering
    references_list = sorted(references_to_process.items())
    total_refs = len(references_list)
    
    # Process references in batches of 10
    batch_size = 5
    
    for i in range(0, total_refs, batch_size):
        batch_dict = dict(references_list[i:i + batch_size])
        try:
            batch_results = process_batch_with_gemini(batch_dict)
            for ref, entries in batch_results.items():
                progress.add(ref, entries)
                # logger.info(f"Processed {ref}")
                # for entry in entries:
                    # logger.info(f"Processed {entry}")
        except Exception as e:
            logger.exception(f"Failed to process batch starting at index {i}: {str(e)}")
            # progress.save()
            raise
            
        logger.info(f"Processed batch {i//batch_size + 1} of {(total_refs + batch_size - 1)//batch_size}")
        
        # if i > 0 and i %  == 0:
        #     progress.save()
        #     logger.info(f"Saved progress at index {i}")

    progress.save()
    logger.info("Processing completed successfully")
