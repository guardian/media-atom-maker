#!/usr/bin/env bash

if [[ $# -lt 1 ]] ; then
    echo 'usage: ./create-dev-stack.sh <USERNAME> [AWS_PROFILE]'
    exit 0
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
USERNAME=$1
AWS_PROFILE=$2

aws cloudformation create-stack --stack-name media-atom-maker-DEV-${USERNAME}  \
    --template-body file://${DIR}/media-atom-maker-dev.yml \
    --profile ${AWS_PROFILE} \
    --region eu-west-1

if [ $? -ne 0 ]; then
    echo "Seems you don't have the correct credentials, try uploading the YML manualy via the console"
else
    echo "Stack media-atom-maker-DEV-${USERNAME} has been created."
fi