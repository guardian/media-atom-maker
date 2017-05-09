#!/usr/bin/env bash

PROFILE='media-service'
PIPELINE_NAME='media-atom-pipeline-DEV'

BUILD_BUCKET='atom-maker-dist'
BUILD_KEY=media-service/DEV/media-atom-upload-actions/media-atom-uploader.zip''

JQ_FILTER='.StackResources[] | select(.ResourceType == "AWS::Lambda::Function") | .PhysicalResourceId'

sbt uploader/packageBin || exit 1

aws s3 cp ./uploader/target/universal/media-atom-uploader.zip s3://${BUILD_BUCKET}/$BUILD_KEY --profile $PROFILE

STACK_JSON=$(aws cloudformation describe-stack-resources --stack-name $PIPELINE_NAME --profile $PROFILE)
LAMBDAS=($(echo $STACK_JSON | jq -r "$JQ_FILTER"))

for LAMBDA in "${LAMBDAS[@]}"; do
  aws lambda update-function-code --function-name $LAMBDA --s3-bucket $BUILD_BUCKET --s3-key $BUILD_KEY --profile $PROFILE
done
