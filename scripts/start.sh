#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

red='\x1B[0;31m'

NODE_MAJOR_VERSION=$(node -v | cut -d "." -f 1)
DESIRED_NODE_VERSION=$(cat "${DIR}/../.nvmrc")

if [[ "${NODE_MAJOR_VERSION}" != *"${DESIRED_NODE_VERSION}"  ]]; then
  echo -e "${red}Your node version ${NODE_MAJOR_VERSION}" does not match "${DESIRED_NODE_VERSION}"
  echo -e "${red}Please run 'nvm use' to get the desired node version"
  exit 1
fi

yarn run build-dev &
./sbt $@ app/run
