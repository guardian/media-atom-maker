#!/usr/bin/env bash

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

red='\x1B[0;31m'
plain='\033[0m'

STATUS=$(aws sts get-caller-identity --profile media-service 2>&1 || true)
if [[ ${STATUS} =~ (ExpiredToken) ]]; then
  echo -e "${red}Credentials for the media-service profile are expired. Please fetch new credentials and run this again.${plain}"
  exit 1
elif [[ ${STATUS} =~ ("could not be found") ]]; then
  echo -e "${red}Credentials for the media-service profile are missing. Please ensure you have the right credentials.${plain}"
  exit 1
fi
