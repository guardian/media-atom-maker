#!/usr/bin/env bash

set -e

yarn install --frozen-lockfile
yarn lint
yarn test
yarn run build
