#!/bin/bash

set -e

setupNvm() {
  export NVM_DIR="$HOME/.nvm"
  if [ -s "$NVM_DIR/nvm.sh" ]; then
    # This loads nvm if installed directly
    source "$NVM_DIR/nvm.sh"
  elif [ -s "$(brew --prefix nvm)/nvm.sh" ]; then
    # This will load nvm if installed via brew
    source "$(brew --prefix nvm)/nvm.sh"
  else
    echo "Can't find NVM"
    exit 1
  fi
}

buildPlutoLambda(){
  pushd pluto-message-ingestion

  # The pluto lambda uses a different version of node from main app.
  # Change node versions and install yarn
  # TODO use the same node version throughout the repo
  nvm install
  nvm use
  npm install -g yarn

  yarn
  yarn build
  popd
}

buildApp() {
  nvm install
  nvm use
  npm install -g yarn

  yarn install --force --non-interactive
  yarn lint
  yarn test
  yarn run build-autotrack
  yarn run build
}

main() {
  setupNvm
  buildPlutoLambda
  buildApp
}

main
