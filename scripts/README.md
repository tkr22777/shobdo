# scripts/

Offline tooling for building and maintaining the Shobdo dictionary database. Nothing here touches production directly — all writes go through the REST API or a local Docker stack.

---

## Structure

```
scripts/
├── python_enrichment/   — main data pipeline (see below)
├── python_webscraping/  — one-off scraper for seeding raw word lists
├── hooks/               — git hooks (pre-commit)
└── generate-sitemap.mjs — generates sitemap.xml from the live word list
```

---

## python_enrichment/

The core pipeline for populating the dictionary. Run from this directory with `poetry run python src/...`.

### Modules

| Module | Purpose |
|--------|---------|
| `src/utils.py` | Shared `load_json` / `save_json` helpers |
| `src/config.py` | API key settings (Gemini, etc.) |
| `src/logger.py` | Colourised logging |
| `src/word_client/` | REST API client — all writes go through here |
| `src/store/` | MongoDB snapshot + rollback transaction context manager |
| `src/extraction/` | Extract word references from raw data; find words missing from the DB |
| `src/generation/` | Generate dictionary entries for missing words via Gemini, then upload |
| `src/discovery/` | Feed lexicographic prefix slices to Gemini to find words missing from the DB entirely |
| `src/cleanup/` | Classify all DB words as `VALID_ROOT` / `LIKELY_INFLECTION` / `GARBAGE` |

### Data access policy

| Operation | How |
|-----------|-----|
| Bulk reads (scan 107k words) | pymongo directly — far cheaper than 1k+ paginated API calls |
| Individual lookups | `WordClient` — handles inflection fallback transparently |
| All writes (create / update / delete) | `WordClient` via REST API — enforces validation and cache invalidation |

### Pipeline stages

```
extraction  →  generation  →  upload
                                  ↑
discovery   ────────────────────┘
```

1. **extraction** — find which words are referenced in data but missing from the DB
2. **generation** — call Gemini to produce meanings for each missing word; multi-worker, resumable
3. **upload** — POST generated entries to the API; resumable via progress file
4. **discovery** — independently discover missing words by querying Gemini per alphabetical prefix slice
5. **cleanup** — classify every word in the DB to flag inflections and garbage for removal

### Key data files (gitignored, on disk only)

| File | Description |
|------|-------------|
| `data/generation/worker_N/` | Per-worker Gemini outputs (`entries`, `discarded`, `inflections`, `meta`) |
| `data/generated_entries.json` | Merged output ready for upload |
| `data/upload_progress.json` | Tracks uploaded words for resumption |
| `data/discovery/prefix_sets.json` | Pre-computed alphabetical slices |
| `data/discovery/discovered_words.json` | Gemini-suggested missing words |
| `data/cleanup/results/<run>/` | Classification results + progress |
| `data/mongodump/snapshots/` | MongoDB gzip snapshots |
