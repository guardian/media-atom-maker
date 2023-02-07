#!/usr/bin/env bash

set -e

yarn install --force --non-interactive
yarn lint
yarn test
yarn run build
