#!/usr/bin/env bash

aws s3 cp s3://atom-maker-conf/TEST/media-atom-maker-integration-test.private.conf integration-tests/src/test/conf/ --profile media-service
