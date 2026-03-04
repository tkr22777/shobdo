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

## Built With

* [Nginx](https://nginx.org/): - Frontend serving vanilla JavaScript with jQuery https://shobdo-1.onrender.com/ [Beta]
* [Play Java Framework](https://www.playframework.com/) - Web Framework for Java and Scala for backend API
* [MongoDB](https://www.mongodb.com/) - Used as the primary datastore for words
* [Redis](https://redis.io/) - Used for caching word search results and definitions (disabled for now)
* [Docker](https://www.docker.com/) - Container platform for development and deployment
