#!/usr/bin/env bash -e

PROFILE='media-service'
PIPELINE_NAME='media-atom-pipeline-DEV'
REGION='eu-west-1'

BUILD_BUCKET='atom-maker-dist'
LAMBDA_PACKAGE_KEY='media-service/DEV/media-atom-upload-actions/media-atom-uploader.zip'

JQ_FILTER='.StackResources[] | select(.ResourceType == "AWS::Lambda::Function") | .PhysicalResourceId'

aws s3 cp ./uploader/target/universal/media-atom-uploader.zip s3://${BUILD_BUCKET}/${LAMBDA_PACKAGE_KEY} --sse aws:kms --profile ${PROFILE} --region ${REGION}

STACK_JSON=$(aws cloudformation describe-stack-resources --stack-name ${PIPELINE_NAME} --profile ${PROFILE} --region ${REGION})
LAMBDAS=($(echo ${STACK_JSON} | jq -r "$JQ_FILTER"))

for LAMBDA in "${LAMBDAS[@]}"; do
  aws lambda update-function-code --function-name ${LAMBDA} --s3-bucket ${BUILD_BUCKET} --s3-key ${LAMBDA_PACKAGE_KEY} --profile ${PROFILE} --region ${REGION} | jq .RevisionId
done
