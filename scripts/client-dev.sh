#!/usr/bin/env bash
printf "\n\rRemoving compiled css file... \n\r\n\r"
rm public/video-ui/build/main.css 2> /dev/null
printf "\n\rStarting Webpack Dev Server... \n\r\n\r"
yarn run client-dev &
printf "\n\rStarting Play App... \n\r\n\r"
JS_ASSET_HOST=https://video-assets.local.dev-gutools.co.uk/assets/ ./sbt $@ app/run
