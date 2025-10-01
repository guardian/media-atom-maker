#!/usr/bin/env bash

set -e

cd asset-management-message-ingestion-lambda


yarn install --frozen-lockfile
yarn test:ci
yarn build

zip -FSjr "dist/asset-management-message-ingestion-lambda.zip" "dist/index.js"

aws lambda update-function-code \
    --function-name asset-management-message-ingestion-lambda-CODE \
    --zip-file fileb://dist/asset-management-message-ingestion-lambda.zip \
    --profile media-service \
    --region eu-west-1