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

## Built With

* [Nginx](https://nginx.org/): - Frontend serving vanilla JavaScript with jQuery https://shobdo-1.onrender.com/ [Beta]
* [Play Java Framework](https://www.playframework.com/) - Web Framework for Java and Scala for backend API
* [MongoDB](https://www.mongodb.com/) - Used as the primary datastore for words
* [Redis](https://redis.io/) - Used for caching word search results and definitions (disabled for now)
* [Docker](https://www.docker.com/) - Container platform for development and deployment
