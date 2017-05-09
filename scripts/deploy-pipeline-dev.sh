#!/usr/bin/env bash

PROFILE='media-service'
PIPELINE_NAME='media-atom-pipeline-DEV'

BUILD_BUCKET='atom-maker-dist'
CONFIG_BUCKET='atom-maker-conf'

CLOUDFORMATION_KEY='media-service/DEV/media-atom-pipeline.yaml'
LAMBDA_PACKAGE_KEY='media-service/DEV/media-atom-upload-actions/media-atom-uploader.zip'

JQ_FILTER='.StackResources[] | select(.ResourceType == "AWS::Lambda::Function") | .PhysicalResourceId'

sbt uploader/packageBin || exit 1

aws s3 cp ./uploader/target/scala-2.11/resource_managed/main/media-atom-pipeline.yaml s3://${BUILD_BUCKET}/${CLOUDFORMATION_KEY} --profile ${PROFILE}
aws s3 cp ./uploader/target/universal/media-atom-uploader.zip s3://${BUILD_BUCKET}/${LAMBDA_PACKAGE_KEY} --profile ${PROFILE}

aws cloudformation update-stack \
  --stack-name ${PIPELINE_NAME} \
  --template-url https://s3-eu-west-1.amazonaws.com/${BUILD_BUCKET}/${CLOUDFORMATION_KEY} \
  --parameters \
    ParameterKey=App,UsePreviousValue=true \
    ParameterKey=BuildBucket,UsePreviousValue=true \
    ParameterKey=ConfigBucket,UsePreviousValue=true \
    ParameterKey=Stack,UsePreviousValue=true \
    ParameterKey=Stage,UsePreviousValue=true \
    ParameterKey=UploadBucketName,UsePreviousValue=true \
    ParameterKey=UploadProgressTable,UsePreviousValue=true \
  --capabilities CAPABILITY_IAM \
  --profile ${PROFILE}

STACK_JSON=$(aws cloudformation describe-stack-resources --stack-name ${PIPELINE_NAME} --profile ${PROFILE})
LAMBDAS=($(echo ${STACK_JSON} | jq -r "$JQ_FILTER"))

for LAMBDA in "${LAMBDAS[@]}"; do
  aws lambda update-function-code --function-name ${LAMBDA} --s3-bucket ${BUILD_BUCKET} --s3-key ${LAMBDA_PACKAGE_KEY} --profile ${PROFILE}
done
