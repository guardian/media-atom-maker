#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

"${DIR}/check-node-version.sh"
yarn run build-dev &
./sbt $@ app/run
