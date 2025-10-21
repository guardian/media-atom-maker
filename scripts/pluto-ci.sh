#!/usr/bin/env bash

set -e

cd pluto-message-ingestion

# The pluto lambda uses a different version of node from main app
# so we run it separately

yarn install --frozen-lockfile

pushd scripts
    yarn install --frozen-lockfile
popd

yarn test:ci
yarn lint:ci
yarn typecheck

yarn build

zip -FSjr "target/pluto-message-ingestion.zip" "target/index.js"