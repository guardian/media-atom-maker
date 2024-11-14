#!/usr/bin/env bash

fileExists() {
  test -e "$1"
}

set_node_version() {

  if ! fileExists "$NVM_DIR/nvm.sh"; then
    node_version=`cat .nvmrc`
    echo -e "${yellow}nvm not found: please ensure you're using node $node_version\r\n"
    echo -e "${nocolour}NVM is not required to run this project, but we recommend using it to easily manage node versions"
    echo -e "Install it from https://github.com/creationix/nvm#installation\r\n\r\n"
  else
    source "$NVM_DIR/nvm.sh"
    nvm use
  fi
}

set_node_version
# printf "\n\rRemoving compiled css file... \n\r\n\r"
# rm video-ui/build/main.css 2> /dev/null
printf "\n\rStarting Vite Dev Server... \n\r\n\r"
yarn run client-dev &
printf "\n\rStarting Play App... \n\r\n\r"
sbt $@ app/run
