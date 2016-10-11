#!/usr/bin/env bash

printf "\n\rSetting up Campaign Central Client Side dependancies... \n\r\n\r"
printf "\n\rInstalling NPM packages... \n\r\n\r"

npm install

printf "\n\Compiling Javascript... \n\r\n\r"

npm run build

printf "\n\rDone.\n\r\n\r"
