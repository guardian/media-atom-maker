#!/usr/bin/env bash

set -e
 # Ensure we don't overwrite existing (Teamcity) builds.
  LAST_TEAMCITY_BUILD=4800
  export GITHUB_RUN_NUMBER=$(( $GITHUB_RUN_NUMBER + $LAST_TEAMCITY_BUILD ))
  sbt clean compile test riffRaffUpload
