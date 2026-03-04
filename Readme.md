# Shobdo

A Bangla Dictionary Webservice

Shobdo ("শব্দ", meaning "word" in Bangla) is a modern dictionary application that provides Bangla word definitions, pronunciations, and related information through a web interface.

## Project Structure

- `/application/shobdo-app`: Play Java backend API service
- `/client/react`: Vite + React frontend (primary UI)
- `/client`: Nginx configuration and legacy HTML frontend
- `/data`: Dictionary data files (MongoDB dump, 42k+ words)
- `/deploy`: Docker Compose and environment configuration
- `/scripts`: Utility scripts

## Getting Started (Local)

### Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- [sbt](https://www.scala-sbt.org/) (to build the backend)
- [Node.js](https://nodejs.org/) 18+ and npm (to build the frontend)
- `make`

### 1. Copy the environment template

```bash
cp deploy/local.env.template deploy/local.env
```

`deploy/local.env` is gitignored and never committed.

### 2. Set up Google OAuth

Sign-in uses Google Identity Services. You need a Google OAuth client ID:

1. Go to [Google Cloud Console](https://console.cloud.google.com) → **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth client ID** → Application type: **Web application**
3. Under **Authorised JavaScript origins**, add:
   - `http://localhost:32779` (local Docker)
   - Your production URL (e.g. `https://www.shobdo.info`)
4. No redirect URIs needed — this uses the GSI popup flow, not redirects
5. Copy the **Client ID** (format: `xxxx.apps.googleusercontent.com`)

Open `deploy/local.env` and set:

```
SHOBDO_GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
```

This single value is used by both the backend (to verify ID tokens) and the frontend (to render the Google sign-in button). `make start-docker-compose` propagates it automatically.

### 3. Build the backend Docker image

This only needs to be re-run when backend source changes.

```bash
cd application/shobdo-app
make build-docker-image-from-source
cd ../..
```

### 4. Start the stack

```bash
make start-docker-compose
```

This will:
- Generate `client/react/.env.local` from `deploy/local.env`
- Install frontend dependencies and run `vite build`
- Start MongoDB, the Play backend, and the Nginx frontend via Docker Compose

The app will be available at **http://localhost:32779**

MongoDB data (42,545 words) is restored automatically on first startup.

### 5. Stop the stack

```bash
make stop-docker-compose
```

---

## Setting Up an Admin User

There is no sign-up flow for elevated roles — the first OWNER must be assigned directly in MongoDB. All subsequent role assignments can be done through the Admin panel in the UI.

**1. Start the stack and sign in at least once** so your user document exists in MongoDB.

**2. Assign the OWNER role directly in MongoDB:**

```bash
docker exec deploy-mongo-1 mongosh Dictionary --eval \
  'db.Users.updateOne({ email: "your@email.com" }, { $set: { role: "OWNER" } })'
```

**3. Sign out and sign back in.** The session will pick up the new role.

An "অ্যাডমিন" link will appear in the site footer. From there you can assign roles to other users via the Admin panel.

**Role hierarchy:**

| Role | Can assign |
|---|---|
| USER | — |
| REVIEWER | — |
| ADMIN | USER, REVIEWER |
| OWNER | USER, REVIEWER, ADMIN, OWNER |

---

## User Request (Contribution) System

Signed-in users can suggest changes to the dictionary. All suggestions are queued as **pending requests** and only take effect after a REVIEWER approves them.

### How it works

```
User submits request → stored as PENDING → REVIEWER approves → change applied to dictionary
```

The request body is stored verbatim. Approval replays it against the live word store.

### Enabling crowdsourcing

1. **Bootstrap an OWNER** (see above)
2. Sign in to the app and open **অ্যাডমিন** in the footer
3. Assign the **REVIEWER** role to trusted users from the Admin panel
4. Signed-in users can now submit suggestions; REVIEWERs will approve them

### Submission flows

| What | Who | How |
|---|---|---|
| Suggest a new word | Any signed-in user | "অবদান" panel in footer → "নতুন শব্দ" tab |
| Suggest a new meaning | Any signed-in user | Open a word → click **✏** next to any meaning or **+ অর্থ যোগ করুন** at the bottom |
| Suggest edits to a meaning | Any signed-in user | Open a word → click **✏** next to the meaning |
| View my submissions | Any signed-in user | "অবদান" panel → "আমার জমা" tab |

> **Note:** Word creation accepts a spelling only — meanings cannot be included in the initial request. Once the word is approved and visible, meanings can be suggested separately via the inline buttons.

### Approval flow (REVIEWER+)

Pending requests are approved via the API:

```
POST /api/v1/requests/:requestId/approve
```

Requires a session with `REVIEWER`, `ADMIN`, or `OWNER` role. Returns `200 OK` on success.
The reviewer's userId is recorded on the request as part of the audit trail.

### API reference

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/requests/words` | signed in | Submit word creation request. Body: `{ "spelling": "শব্দ" }` |
| `PUT` | `/api/v1/requests/words/:wordId` | signed in | Submit word update request. Body: Word JSON (spelling only — meanings are not updated via this endpoint) |
| `POST` | `/api/v1/requests/words/:wordId/meanings` | signed in | Submit meaning creation request. Body: `{ "text": "...", "partOfSpeech": "...", "exampleSentence": "..." }` |
| `PUT` | `/api/v1/requests/words/:wordId/meanings` | signed in | Submit meaning update request. Body: Meaning JSON with `id` field set |
| `DELETE` | `/api/v1/requests/words/:wordId/meanings/:meaningId` | signed in | Submit meaning deletion request |
| `GET` | `/api/v1/requests/mine` | signed in | List the caller's own pending requests |
| `GET` | `/api/v1/requests/:requestId` | any | Get a specific request by id |
| `POST` | `/api/v1/requests/:requestId/approve` | REVIEWER+ | Approve and apply a pending request |

### Request lifecycle

A request is **ACTIVE** (pending) until it is approved. On approval:
- The change is written to the word store
- The request status is flipped to **DELETED** (used as "merged/closed") and the approver's id is recorded

There is currently no reject/dismiss endpoint — requests stay pending until explicitly approved or manually removed from MongoDB.

---

## Built With

* [Play Java Framework](https://www.playframework.com/) — Backend API (Java)
* [React 18](https://react.dev/) + [Vite](https://vitejs.dev/) — Frontend UI
* [Nginx](https://nginx.org/) — Static file serving and reverse proxy
* [MongoDB](https://www.mongodb.com/) — Primary datastore for words
* [Redis](https://redis.io/) — Caching (infrastructure wired, currently disabled)
* [Docker](https://www.docker.com/) — Container platform for development and deployment
* [Google Identity Services](https://developers.google.com/identity) — User authentication
