"""
client.py — WordClient: a thin CRUD wrapper around the Shobdo REST API.

All methods raise WordClientError (or its subclasses) on failure.
The client holds a requests.Session for connection reuse.

Usage:
    from src.word_client.client import WordClient, WordNotFoundError

    client = WordClient("http://localhost:32779")

    # Look up a word
    word = client.get_by_spelling("শব্দ")
    print(word["id"], word["spelling"])

    # Create + add a meaning
    word = client.create("নতুন")
    word = client.add_meaning(word["id"], {"text": "নতুন অর্থ", "partOfSpeech": "ADJECTIVE"})

    # Update / delete a meaning
    meaning_id = next(iter(word["meanings"]))
    word = client.update_meaning(word["id"], meaning_id, {"text": "updated"})
    client.delete_meaning(word["id"], meaning_id)

    # Synonym / antonym helpers
    client.add_synonym(word["id"], meaning_id, "সমার্থক")
    client.remove_synonym(word["id"], meaning_id, "সমার্থক")

    # Inflections
    client.add_inflections(word["id"], [{"spelling": "শব্দের", "type": "GENITIVE"}])

    # Search
    results = client.search("শব্দ")
"""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

import urllib.parse

import requests

from src.logger import setup_logger
from src.word_client.models import InflectionRecord, MeaningRecord, WordRecord

logger = setup_logger("word_client")

DEFAULT_BASE_URL = "http://localhost:32779"
DEFAULT_TIMEOUT = 30


# ---------------------------------------------------------------------------
# Exceptions
# ---------------------------------------------------------------------------

class WordClientError(Exception):
    """Base class for all WordClient errors."""


class WordNotFoundError(WordClientError):
    """Raised when a word or meaning does not exist (HTTP 404)."""


class WordConflictError(WordClientError):
    """Raised when a word already exists (HTTP 409 / 400 duplicate)."""


class WordAPIError(WordClientError):
    """Raised for unexpected non-2xx responses."""
    def __init__(self, method: str, url: str, status: int, body: str):
        super().__init__(f"{method} {url} → HTTP {status}: {body[:200]}")
        self.status = status


# ---------------------------------------------------------------------------
# Client
# ---------------------------------------------------------------------------

class WordClient:
    """
    CRUD client for the Shobdo REST API.

    All write methods return the full updated WordRecord so callers always
    have the latest server state without a follow-up GET.
    """

    def __init__(self, base_url: str = DEFAULT_BASE_URL, timeout: int = DEFAULT_TIMEOUT):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self._session = requests.Session()
        self._session.headers.update({"Content-Type": "application/json", "Accept": "application/json"})

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    def _url(self, path: str) -> str:
        return f"{self.base_url}{path}"

    def _raise_for_status(self, resp: requests.Response, method: str) -> None:
        if resp.status_code == 404:
            raise WordNotFoundError(f"{method} {resp.url} → 404")
        if resp.status_code in (400, 409):
            raise WordConflictError(f"{method} {resp.url} → {resp.status_code}: {resp.text[:200]}")
        if not resp.ok:
            raise WordAPIError(method, resp.url, resp.status_code, resp.text)

    def _get(self, path: str) -> dict:
        resp = self._session.get(self._url(path), timeout=self.timeout)
        self._raise_for_status(resp, "GET")
        return resp.json()

    def _post(self, path: str, body: dict) -> dict:
        resp = self._session.post(self._url(path), json=body, timeout=self.timeout)
        self._raise_for_status(resp, "POST")
        return resp.json()

    def _put(self, path: str, body: dict) -> dict:
        resp = self._session.put(self._url(path), json=body, timeout=self.timeout)
        self._raise_for_status(resp, "PUT")
        return resp.json()

    def _delete(self, path: str) -> None:
        resp = self._session.delete(self._url(path), timeout=self.timeout)
        self._raise_for_status(resp, "DELETE")

    # ------------------------------------------------------------------
    # Word — read
    # ------------------------------------------------------------------

    def get_by_id(self, word_id: str) -> WordRecord:
        """GET /api/v1/words/:id"""
        return self._get(f"/api/v1/words/{word_id}")

    def get_by_spelling(self, spelling: str) -> WordRecord:
        """
        GET /api/v1/bn/word/:spelling

        Returns the root word even when *spelling* is an inflection
        (the response will include an ``inflectedFrom`` field in that case).
        Raises WordNotFoundError if not found at all.
        """
        encoded = urllib.parse.quote(spelling, safe="")
        return self._get(f"/api/v1/bn/word/{encoded}")

    def search(self, query: str) -> list[WordRecord]:
        """POST /api/v1/words/search — returns a list of matching WordRecords."""
        result = self._post("/api/v1/words/search", {"spelling": query})
        # API returns either a list or a wrapper object — normalise
        if isinstance(result, list):
            return result
        return result.get("words", result.get("results", []))

    def list_words(self, start_word_id: str | None = None, limit: int = 100) -> list[WordRecord]:
        """GET /api/v1/words — paginated listing."""
        params = {"limit": limit}
        if start_word_id:
            params["startWordId"] = start_word_id
        resp = self._session.get(self._url("/api/v1/words"), params=params, timeout=self.timeout)
        self._raise_for_status(resp, "GET")
        result = resp.json()
        if isinstance(result, list):
            return result
        return result.get("words", [])

    # ------------------------------------------------------------------
    # Word — write
    # ------------------------------------------------------------------

    def create(self, spelling: str) -> WordRecord:
        """POST /api/v1/words — create a new word entry."""
        return self._post("/api/v1/words", {"spelling": spelling})

    def get_or_create(self, spelling: str) -> WordRecord:
        """POST /api/v1/words/postget — return existing word or create it."""
        return self._post("/api/v1/words/postget", {"spelling": spelling})

    def update(self, word_id: str, data: dict) -> WordRecord:
        """PUT /api/v1/words/:id — update top-level word fields."""
        return self._put(f"/api/v1/words/{word_id}", data)

    def delete(self, word_id: str) -> None:
        """DELETE /api/v1/words/:id"""
        self._delete(f"/api/v1/words/{word_id}")

    # ------------------------------------------------------------------
    # Meanings
    # ------------------------------------------------------------------

    def list_meanings(self, word_id: str) -> dict[str, MeaningRecord]:
        """GET /api/v1/words/:wordId/meanings"""
        return self._get(f"/api/v1/words/{word_id}/meanings")

    def get_meaning(self, word_id: str, meaning_id: str) -> MeaningRecord:
        """GET /api/v1/words/:wordId/meanings/:meaningId"""
        return self._get(f"/api/v1/words/{word_id}/meanings/{meaning_id}")

    def add_meaning(self, word_id: str, meaning: dict) -> WordRecord:
        """
        POST /api/v1/words/:wordId/meanings

        *meaning* dict fields (all optional except ``text``):
          text, partOfSpeech, synonyms, antonyms, exampleSentence
        Returns the full updated WordRecord.
        """
        return self._post(f"/api/v1/words/{word_id}/meanings", meaning)

    def update_meaning(self, word_id: str, meaning_id: str, data: dict) -> WordRecord:
        """PUT /api/v1/words/:wordId/meanings/:meaningId — returns updated WordRecord."""
        return self._put(f"/api/v1/words/{word_id}/meanings/{meaning_id}", data)

    def delete_meaning(self, word_id: str, meaning_id: str) -> None:
        """DELETE /api/v1/words/:wordId/meanings/:meaningId"""
        self._delete(f"/api/v1/words/{word_id}/meanings/{meaning_id}")

    # ------------------------------------------------------------------
    # Synonyms / antonyms
    # ------------------------------------------------------------------

    def add_synonym(self, word_id: str, meaning_id: str, synonym: str) -> WordRecord:
        """POST /api/v1/words/:wordId/meanings/:meaningId/synonym/add"""
        return self._post(
            f"/api/v1/words/{word_id}/meanings/{meaning_id}/synonym/add",
            {"synonym": synonym},
        )

    def remove_synonym(self, word_id: str, meaning_id: str, synonym: str) -> WordRecord:
        """POST /api/v1/words/:wordId/meanings/:meaningId/synonym/remove"""
        return self._post(
            f"/api/v1/words/{word_id}/meanings/{meaning_id}/synonym/remove",
            {"synonym": synonym},
        )

    def add_antonym(self, word_id: str, meaning_id: str, antonym: str) -> WordRecord:
        """POST /api/v1/words/:wordId/meanings/:meaningId/antonym/add"""
        return self._post(
            f"/api/v1/words/{word_id}/meanings/{meaning_id}/antonym/add",
            {"antonym": antonym},
        )

    def remove_antonym(self, word_id: str, meaning_id: str, antonym: str) -> WordRecord:
        """POST /api/v1/words/:wordId/meanings/:meaningId/antonym/remove"""
        return self._post(
            f"/api/v1/words/{word_id}/meanings/{meaning_id}/antonym/remove",
            {"antonym": antonym},
        )

    # ------------------------------------------------------------------
    # Inflections
    # ------------------------------------------------------------------

    def add_inflections(self, word_id: str, inflections: list[InflectionRecord]) -> WordRecord:
        """PUT /api/v1/words/:id/inflections — append inflections to a root word."""
        return self._put(f"/api/v1/words/{word_id}/inflections", inflections)

    # ------------------------------------------------------------------
    # Cleanup
    # ------------------------------------------------------------------

    def close(self) -> None:
        self._session.close()

    def __enter__(self):
        return self

    def __exit__(self, *_):
        self.close()
