Notice:
=======

The service is now beta. Cool! If you want to contribute, follow along.

Install Java8 SDK.

Check the `Makefile` and use `make` commands to run/test/build.
Warning: Some makefile changes recreaetes MongoDB and Redis containers

If you want to import to IntelliJ and develop on IntelliJ:
- Use java 8 when importing to IntelliJ. 
- Install routes file plugin on IntelliJ.
- Allow annotation processing on IntelliJ.
- Spin up the mongo docker from scripts directory 
- All tests should now pass!

Some useful requests:
    
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
