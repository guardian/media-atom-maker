#!/usr/bin/env bash

mkdir -p /etc/gu/

aws s3 cp s3://atom-maker-conf/DEV/media-atom-maker.private.conf /etc/gu/media-atom-maker.private.conf --profile media-service
