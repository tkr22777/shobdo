# Shobdo

A Bangla Dictionary Webservice

Shobdo ("শব্দ", meaning "word" in Bangla) is a modern dictionary application that provides Bangla word definitions, pronunciations, and related information through a web interface.

## Getting Started

Microservices can be found in the application directory.

Current list of services:
- shobdo-app: A Java-based dockerized dictionary service

## Project Structure

- `/application`: Contains the core backend services
- `/client`: Frontend implementation based on Nginx with vanilla JavaScript and jQuery
- `/data`: Storage for dictionary data files
- `/deploy`: Deployment configuration files
- `/scripts`: Utility scripts for development and deployment

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

## User Request (Contribution) System

Signed-in users can suggest changes to the dictionary. All suggestions are queued as **pending requests** and only take effect after a REVIEWER approves them.

### How it works

```
User submits request → stored as PENDING → REVIEWER approves → change applied to dictionary
```

The request body is stored verbatim. Approval replays it against the live word store.

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

## Built With

* [Nginx](https://nginx.org/): - Frontend serving vanilla JavaScript with jQuery https://shobdo-1.onrender.com/ [Beta]
* [Play Java Framework](https://www.playframework.com/) - Web Framework for Java and Scala for backend API
* [MongoDB](https://www.mongodb.com/) - Used as the primary datastore for words
* [Redis](https://redis.io/) - Used for caching word search results and definitions (disabled for now)
* [Docker](https://www.docker.com/) - Container platform for development and deployment
