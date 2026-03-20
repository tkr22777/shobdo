"""
models.py — TypedDicts mirroring the Shobdo REST API response shapes.

These are not validated at runtime — they exist to make type hints readable
and serve as living documentation of the API contract.
"""

from typing import Optional
from typing_extensions import TypedDict


class MeaningRecord(TypedDict, total=False):
    id: str
    text: str
    partOfSpeech: Optional[str]     # NOUN, VERB, ADJECTIVE, ADVERB, ...
    synonyms: list[str]
    antonyms: list[str]
    exampleSentence: Optional[str]
    status: str                     # ACTIVE, DRAFT, PENDING, DELETED


class InflectionRecord(TypedDict, total=False):
    spelling: str
    type: str                       # e.g. GENITIVE, PLURAL, PAST_TENSE, ...
    meaning: Optional[str]
    exampleSentence: Optional[str]
    synonyms: list[str]
    antonyms: list[str]


class WordRecord(TypedDict, total=False):
    id: str
    spelling: str
    status: str                         # ACTIVE, DRAFT, DELETED
    meanings: dict[str, MeaningRecord]  # meaningId → MeaningRecord
    inflections: Optional[list[InflectionRecord]]
    inflectedFrom: Optional[str]        # present when GET /bn/word/:spelling returns a root fallback
