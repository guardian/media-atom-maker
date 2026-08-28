#!/usr/bin/env bash

set -e
echo "Need sudo permission to create /etc/gu folder. You may be asked for your password."
sudo mkdir -p /etc/gu/
sudo chown $(whoami) -R /etc/gu/

aws s3 cp s3://atom-maker-conf/DEV/media-atom-maker.private.conf /etc/gu/media-atom-maker.private.conf --profile media-service
aws s3 cp s3://atom-maker-conf/CODE/youtube-service-account.json "$HOME"/.gu/youtube-service-account.json --profile media-service
