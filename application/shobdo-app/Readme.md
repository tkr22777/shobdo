Notice:
=======

The service is in dev mode. Things are (maybe) broken. 
TODO: fix everything and update this readme.

Cool! If you a dev, follow along.

Check the `Makefile` and use `make` commands to run/test/build.

If you want to import to IntelliJ and develop on IntelliJ:
- Use java 8 when importing to IntelliJ. 
- Install routes file plugin on IntelliJ.
- Allow annotation processing on IntelliJ.
- Spin up the mongo docker from scripts directory 
- All tests should now pass!

Some useful requests:
    
    curl -H "Content-Type: application/json" -X POST -d '{"wordCount": "200" }' http://localhost:32779/api/v1/generate
    
    IP="localhost"
    PORT="9000"
    echo "curl sanity checks:"
    
    echo "Testing GET home route:"
    curl -X GET http://localhost:9000/api/v1
    echo ""
    
    Generating a test dictionary
    echo "Testing Temp Dictionary Generate:"
    curl -H "Content-Type: application/json" -X POST -d '{"wordCount":"100"}' http://localhost:9000/api/v1/generate
    echo ""
    
    Serch word by spelling in the dictionary
    echo "Testing Search POST:"
    curl -H "Content-Type: application/json" -X POST -d '{"searchString":"ঙ"}' http://localhost:9000/api/v1/words/search
    echo ""
    
    Get word by spelling in the dictionary
    echo "Testing get word POST:"
    curl -H "Content-Type: application/json" -X POST -d '{"wordSpelling":"ঙ"}' http://localhost:9000/api/v1/words/postget
    echo ""


<!---
The Shobdo webservice:
==============================

Controllers:
============

Application Logic:
==================

Caches:
=======

- WordCache.java:

  Uses redis to words and search results
  
Data Access Objects (DAOs):
===========================

- WordDao.java:
  Interface to data layer for word object's CRUDL and search operations

Objects:
========

- Filters.java:

  Creates the list of HTTP filters used by your application.

- ExampleFilter.java

  A simple filter that adds a header to every response.
--->
