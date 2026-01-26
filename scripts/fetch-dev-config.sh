#!/usr/bin/env bash

mkdir -p /etc/gu/

aws s3 cp s3://atom-maker-conf/CODE/media-atom-maker.private.conf /etc/gu/media-atom-maker.private.conf --profile media-service
aws s3 cp s3://atom-maker-conf/CODE/youtube-service-account.json "$HOME"/.gu/youtube-service-account.json --profile media-service
