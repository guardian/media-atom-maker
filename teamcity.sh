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
  echo "##teamcity[compilationStarted compiler='pluto-lambda']"
  pushd pluto-message-ingestion

  # The pluto lambda uses a different version of node from main app.
  # Change node versions and install yarn
  # TODO use the same node version throughout the repo
  nvm install
  nvm use
  npm install -g yarn

  yarn
  yarn build

  #This should fail if the zip doesn't exist
  ls -lah ./target/riffraff/packages/pluto-message-ingestion/pluto-message-ingestion.zip

  popd
  echo "##teamcity[compilationFinished compiler='pluto-lambda']"
}

buildJsApp() {
  echo "##teamcity[compilationStarted compiler='js-app']"
  nvm install
  nvm use
  npm install -g yarn

  yarn install --force --non-interactive
  yarn lint
  yarn test
  yarn run build-autotrack
  yarn run build
  echo "##teamcity[compilationFinished compiler='js-app']"
}

buildScalaApp() {
  echo "##teamcity[compilationStarted compiler='sbt']"
  sbt clean compile test riffRaffUpload
  echo "##teamcity[compilationFinished compiler='sbt']"
}

main() {
  setupNvm
  buildPlutoLambda
  buildJsApp
  buildScalaApp
}

main
