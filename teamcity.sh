#!/bin/bash

set -e

install_yarn() {
  npm install -g yarn
}

js_pluto_lambda(){
  pushd pluto-message-ingestion
  yarn install
  yarn run build
  popd
}

js_deps() {
  yarn install --force --non-interactive
}

js_test() {
  yarn lint
  yarn test
}

js_build() {
  # angular app
  yarn run build-angular

  # react app
  yarn run build
}

main() {
  install_yarn
  js_pluto_lambda
  js_deps
  js_test
  js_build
}

main
