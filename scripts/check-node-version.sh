#!/usr/bin/env bash

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

red='\x1B[0;31m'

fileExists() {
  test -e "$1"
}

changeNodeVersion() {
    BREW_NVM_SH="$(brew --prefix nvm)/nvm.sh"

    if fileExists "$BREW_NVM_SH"; then
        echo -e "${red}NVM was installed using brew, this is unsupported."
        echo -e "Uninstall it using brew uninstall nvm and then"
        echo -e "Install it from https://github.com/creationix/nvm#installation${plain}"
        exit 1
    fi

    if [[ -z "${NVM_DIR}" ]]; then
        echo -e "${red}NVM not found. NVM is required to run this project"
        echo -e "Install it from https://github.com/creationix/nvm#installation${plain}"
        exit 1
    else
        [ -s "$NVM_DIR/nvm.sh" ] && \. "${NVM_DIR}/nvm.sh"
        # nvm will need to be sourced
        nvm install
    fi
}

checkNodeMatchesNvm() {
    NODE_MAJOR_VERSION=$(node -v | cut -d "." -f 1)
    DESIRED_NODE_VERSION=$(cat "${DIR}/../.nvmrc")

    if [[ "v${DESIRED_NODE_VERSION}" != "${NODE_MAJOR_VERSION}"* ]]; then
        echo -e "${red}Your node version ${NODE_MAJOR_VERSION}" does not match "${DESIRED_NODE_VERSION}"
        echo -e "${red}Please run 'nvm use' to get the desired node version"
        exit 1
    fi
}

changeNodeVersion
checkNodeMatchesNvm # Just in case something didn't work in the nvm switch
