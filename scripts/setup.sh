#!/usr/bin/env bash

printf "\n\rSetting up Media Atom Maker dependencies... \n\r\n\r"
printf "\n\rInstalling NPM packages... \n\r\n\r"

npm install

printf "\n\Compiling Javascript... \n\r\n\r"

npm run build

printf "\n\rDone.\n\r\n\r"
