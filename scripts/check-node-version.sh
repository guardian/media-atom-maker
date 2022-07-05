#!/usr/bin/env bash

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

red='\x1B[0;31m'
plain='\033[0m'

checkNodeMatchesNvm() {
    NODE_MAJOR_VERSION=$(node -v | cut -d "." -f 1)
    DESIRED_NODE_VERSION=$(cat "${DIR}/../.nvmrc")

    if [[ "v${DESIRED_NODE_VERSION}" != "${NODE_MAJOR_VERSION}"* ]]; then
        echo -e "${red}Your node version ${NODE_MAJOR_VERSION} does not match the version the project expects (v${DESIRED_NODE_VERSION})."
        echo -e "Please ensure it matches before starting the project."
        echo -e "If you are using nvm, for example, run \`nvm use\`."
        echo -e "https://github.com/creationix/nvm${plain}"
        exit 1
    fi
}

checkNodeMatchesNvm
