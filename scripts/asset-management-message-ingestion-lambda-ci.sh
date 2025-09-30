#!/usr/bin/env bash

set -e

cd asset-management-message-ingestion-lambda


yarn install --frozen-lockfile
yarn test:ci
yarn build

zip -FSjr "dist/asset-management-message-ingestion-lambda.zip" "dist/index.js"