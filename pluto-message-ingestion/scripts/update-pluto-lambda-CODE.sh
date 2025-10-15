#!/usr/bin/env bash

yarn build

zip -FSjr "target/pluto-message-ingestion.zip" "target/index.js"

aws lambda update-function-code \
    --function-name pluto-message-ingestion-CODE \
    --profile media-service \
    --region eu-west-1 \
    --zip-file "fileb://target/pluto-message-ingestion.zip"