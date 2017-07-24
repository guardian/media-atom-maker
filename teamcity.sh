#!/bin/bash

set -e

NODE_VERSION="6.2.1"

use_nvm() {
  export NVM_DIR="$HOME/.nvm"
  [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"  # This loads nvm

  nvm use ${NODE_VERSION}
}

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

sbt_build() {
  java -Xmx1024m \
    -XX:MaxPermSize=256m \
    -XX:ReservedCodeCacheSize=128m \
    -XX:+CMSClassUnloadingEnabled \
    -Dsbt.log.noformat=true \
    -jar sbt-launcher.jar \
    clean compile test riffRaffUpload
}

main() {
  use_nvm
  install_yarn
  js_pluto_lambda
  js_deps
  js_test
  js_build
  sbt_build
}

main
