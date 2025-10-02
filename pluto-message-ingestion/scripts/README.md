# Pluto ingestion Lambda remote test script

A script to post an 'update project' message to the CODE Pluto ingestion Lambda in AWS, and then check that the project is inserted into the CODE database.

## Running instructions

**nb. Requires Janus credentials to be in your shell.**

```sh
# from pluto-message-ingestion/scripts directory
./test-pluto-lambda-CODE.js

# or, for more verbose output:
./test-pluto-lambda-CODE.js --verbose
```
