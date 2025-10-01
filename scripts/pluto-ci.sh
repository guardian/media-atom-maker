#!/usr/bin/env bash

set -e

cd pluto-message-ingestion

# The pluto lambda uses a different version of node from main app
# so we run it separately

yarn install --frozen-lockfile
yarn test:ci
yarn build

zip -FSjr "target/pluto-message-ingestion.zip" "target/index.js"