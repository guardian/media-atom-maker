#!/usr/bin/env bash

set -e

cd pluto-message-ingestion

# The Pluto lambda has its own Yarn project, so we run its checks separately.

yarn install --frozen-lockfile

pushd scripts
    yarn install --frozen-lockfile
popd

yarn test:ci
yarn lint:ci
yarn typecheck

yarn build

zip -FSjr "target/pluto-message-ingestion.zip" "target/index.js"