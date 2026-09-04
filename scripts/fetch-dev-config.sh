#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

echo "Need sudo permission to create /etc/gu folder. You may be asked for your password."
sudo mkdir -p /etc/gu/
sudo chown -R $(whoami) /etc/gu/

"${DIR}/check-credentials.sh"

aws s3 cp s3://atom-maker-conf/DEV/media-atom-maker.private.conf /etc/gu/media-atom-maker.private.conf --profile media-service
aws s3 cp s3://atom-maker-conf/CODE/youtube-service-account.json "$HOME"/.gu/youtube-service-account.json --profile media-service
