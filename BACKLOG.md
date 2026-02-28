# Shobdo — Backlog

Feature ideas, technical debt, and future improvements.

---

## Authentication & Sessions

### [ ] Implement Google OAuth login
- Use Google Identity Services JS SDK (frontend, just a `<script>` tag)
- Backend: `POST /api/v1/auth/google` — receive ID token, verify with
  `google-auth-library-java`, return a session token
- Store user profile (email, name, Google subject ID) in MongoDB
- Start with Play's signed cookie session (stateless, works for single instance)

### [ ] Move session storage to Redis
- **Why:** Play's signed cookie session is stateless — logout cannot truly
  invalidate a token, and sessions cannot be revoked (e.g. "sign out of all
  devices"). In a multi-instance deployment, stateless sessions work fine for
  auth but offer no server-side control.
- **How:** Generate a random session ID on login → store
  `session:<id> → { userId, email, createdAt }` in Redis with a TTL (e.g. 7 days).
  On each request, look up the session ID in Redis. Logout = delete the key.
- **Infrastructure:** Redis is already in `deploy/docker-compose.yml`, just
  commented out. Re-enable it and point `SHOBDO_REDIS_HOSTNAME` at it.
- **When:** Before scaling to multiple backend instances, or when true
  logout / session revocation is needed.

---

## Search & Discovery

### [ ] Autocomplete / typeahead
- New endpoint: `GET /api/v1/words/suggest?q=<prefix>`
- Return top N prefix matches as a lightweight JSON array
- Render as a dropdown below the search box while typing

### [ ] Fuzzy / phonetic search
- Handle common typos and near-spellings (e.g. "আমর" → "আমার")
- MongoDB text index or custom Soundex/Metaphone matching

### [ ] Search by meaning
- Full-text search across definition text, not just spelling
- MongoDB `$text` index on the `meanings.*.text` field

---

## Data & Content

### [ ] Richer word schema
- Add `partOfSpeech`, `pronunciation` (IPA), `difficulty`, `register`
  (formal / colloquial / archaic) to the Meaning object

### [ ] Word of the Day — deterministic seed
- Current WOTD is random-per-load (cached in localStorage by date)
- A server-side date-seeded endpoint would ensure all users see the
  same word on the same day regardless of device/browser

---

## Infrastructure

### [ ] Re-enable Redis caching
- Word cache (already implemented in `WordCache`) was disabled
- Re-enabling reduces MongoDB load on popular words

### [ ] API rate limiting
- No rate limiting on search or meaning endpoints currently
- Add per-IP limits to prevent abuse

### [ ] Health check improvements
- `health-check.html` exists on the frontend
- Backend `/api/v1/health` could return DB + cache connectivity status
  rather than a static response
