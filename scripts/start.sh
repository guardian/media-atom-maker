#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

"${DIR}/check-node-version.sh"
yarn run build-dev &
./sbt $@ app/run

# catches script exit events and kills the `yarn run build-dev` process and its children
# https://stackoverflow.com/a/22644006
trap "exit" INT TERM
trap "kill 0" EXIT
