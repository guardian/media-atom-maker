#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

for arg in "$@"; do
    if [ "$arg" == "--debug" ]; then
      IS_DEBUG=1
      shift
    fi
done

"${DIR}/check-node-version.sh"
yarn run client-dev &

if [ "$IS_DEBUG" == 1 ]; then
  RELOADING=HOT sbt -jvm-debug 9100 $@ app/run
else
  RELOADING=HOT sbt $@ app/run
fi

# catches script exit events and kills the `yarn run build` process and its children
# https://stackoverflow.com/a/22644006
trap "exit" INT TERM
trap "kill 0" EXIT
