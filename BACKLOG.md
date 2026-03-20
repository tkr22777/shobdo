# Shobdo — Backlog

Feature ideas, technical debt, and future improvements.
GitHub issues are the source of truth — this file is a human-readable summary.

---

## Active / In Progress

### [ ] Inflection data pipeline — #86
All code is live (API + UI), but no words have inflection data yet.
1. Run Gemini enrichment to generate `inflections.json` for ~107k words
2. Write bulk upload script (doesn't exist yet)
3. Upload to Atlas via `PUT /api/v1/words/:id/inflections`

### [ ] Configure Google OAuth credentials — #85
All backend + frontend plumbing is done. Just needs real credentials:
- Create OAuth Client ID at Google Cloud Console
- Set `VITE_GOOGLE_CLIENT_ID` + `SHOBDO_GOOGLE_CLIENT_ID` in local env and Render

---

## Data & Discovery

### [ ] Word discovery script — #87
`src/discovery/query_prefix_sets.py` is built but paused mid-tuning.
Fix: reword end-of-prompt to stop padding. Then run 895 prefix sets.

---

## Search & Discovery

### [ ] Autocomplete / typeahead — #89
New endpoint: `GET /api/v1/words/suggest?q=<prefix>` → dropdown while typing.

### [ ] Fuzzy / phonetic search
Handle common typos and near-spellings (e.g. "আমর" → "আমার").

### [ ] Search by meaning
Full-text search across definition text. MongoDB `$text` index on `meanings.*.text`.

---

## Infrastructure

### [ ] Re-enable Redis caching — #88
`WordCache` is implemented. Redis is in docker-compose (commented out). Just re-enable.

### [ ] API rate limiting — #90
No rate limiting exists. Start with Nginx `limit_req_zone` — no code changes needed.

### [ ] Move session storage to Redis
Before scaling to multiple backend instances. Current signed-cookie sessions can't
be server-side revoked. Redis already in docker-compose.

### [ ] Health check improvements
`/api/v1/health` should return DB + Redis connectivity status, not a static response.

### [ ] Address Dependabot security alerts — #70
GitHub flagged 15 vulnerabilities (1 critical, 7 high, 7 moderate) as of 2026-03-19.
See: https://github.com/tkr22777/shobdo/security/dependabot

---

## Content & Moderation

### [ ] Vulgarity classification + user filter
Add a `vulgarityLevel` field to the Word schema (e.g. `NONE`, `MILD`, `EXPLICIT`).
- **Data**: classify existing 107k words via AI (fits naturally into the cleanup pipeline)
- **Backend**: store level on Word; filter out `EXPLICIT` (or configurable threshold) in search results when requested
- **Settings**: toggle in সেটিংস page — "অশ্লীল শব্দ দেখাও / লুকাও"; preference saved to localStorage
- **Default**: explicit words hidden by default

---

## UX / Polish

### [ ] PWA install prompt — surface "Add to Home Screen"
PWA manifest + service worker are already in place (app is installable).
Add an in-app install prompt so users on mobile are actively nudged to save
the app to their home screen rather than relying on the browser's buried menu.

---

## Data Schema

### [ ] Richer word schema
Add `pronunciation` (IPA), `difficulty`, `register` (formal/colloquial/archaic)
to the Meaning object.

---

## Done (for reference)

- [x] Google Sign-In backend + frontend plumbing (needs credentials — see #85)
- [x] Multi-role user system (USER / REVIEWER / ADMIN / OWNER)
- [x] Inflection support — backend schema, API, frontend UI (no data yet — see #86)
- [x] Random word pool — O(1) surprise serving, 2000-word in-memory pool
- [x] Word enrichment pipeline — 107,943 words in Atlas (was 42,545)
- [x] Contribution UI — new word form, inline meaning edit, my submissions
- [x] Reviewer approval UI
- [x] Word of the Day — date-seeded, same word for all users on a given day
- [x] OG preview cards for social bots (word URLs)
- [x] PWA support — installable on iOS and Android
- [x] Remove GitHub repo link from footer
- [x] Clickable synonym / antonym tags
- [x] Custom domain — shobdo.info live on Render
- [x] React + Vite frontend migration
- [x] Sitemap (107,944 URLs)
- [x] partOfSpeech badge on meanings
