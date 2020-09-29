#!/usr/bin/env bash

set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"


printf "\n\rSetting up Media Atom Maker dependencies... \n\r\n\r"

check_node_version() {
  "${DIR}/check-node-version.sh"
}

installed() {
  hash "$1" 2>/dev/null
}

install_yarn() {
  if ! installed yarn; then
    echo 'Installing yarn'
    npm install -g yarn
  fi
}

setup_nginx() {
  dev-nginx setup-app ${DIR}/../nginx/nginx-mappings.yml
}

install_deps_and_build() {
  yarn install
  printf "\n\Compiling Javascript... \n\r\n\r"
  yarn build
}

main() {
  check_node_version
  install_yarn
  setup_nginx
  install_deps_and_build
  printf "\n\rDone.\n\r\n\r"
}

main
