#!/usr/bin/env bash
printf "\n\rRemoving compiled css file... \n\r\n\r"
rm public/video-ui/build/main.css
printf "\n\rStarting Webpack Dev Server... \n\r\n\r"
npm run client-dev &
printf "\n\rStarting Play App... \n\r\n\r"
JS_ASSET_HOST=https://media-atom-maker-assets.local.dev-gutools.co.uk/assets/ sbt run -Dhttp.port=9001
