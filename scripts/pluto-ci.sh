#!/usr/bin/env bash

set -e

cd pluto-message-ingestion

# The pluto lambda uses a different version of node from main app
# so we run it separately

yarn
yarn build
