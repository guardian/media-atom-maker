#!/usr/bin/env bash

set -e

yarn install --frozen-lockfile
yarn lint
yarn format:check
yarn test
yarn run build
