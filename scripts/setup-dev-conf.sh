#!/usr/bin/env bash
cd "${0%/*}"

if [ $# -ne 3 ]
then
  echo "usage: $0 <STACK_NAME> <PANDA_AWS_PROFILE> <DOMAIN>"
  exit 1
fi

STACK_NAME=$1
PANDA_AWS_PROFILE=$2
DOMAIN=$3

# always use the media-service account and eu-west-1
MEDIA_AWS_PROFILE=media-service
REGION=eu-west-1

function get_resource() {
    aws cloudformation describe-stack-resources \
        --stack-name ${STACK_NAME} --profile ${MEDIA_AWS_PROFILE} --region ${REGION} \
        | jq ".StackResources[] | select(.LogicalResourceId == \"$1\") | .PhysicalResourceId" | tr -d '"'
}

LIVE_STREAM_NAME=$(get_resource "MediaAtomLiveKinesisStream")
PREVIEW_STREAM_NAME=$(get_resource "MediaAtomPreviewKinesisStream")

PREVIEW_REINDEX_STREAM_NAME=${PREVIEW_STREAM_NAME}
PUBLISHED_REINDEX_STREAM_NAME=${LIVE_STREAM_NAME}

DYNAMO_TABLE=$(get_resource "MediaAtomsDynamoTable")
DYNAMO_PUBLISHED_TABLE=$(get_resource "PublishedMediaAtomsDynamoTable")
DYNAMO_AUDIT_TABLE=$(get_resource "AuditMediaAtomMakerDynamoTable")

sed -e "s/{DOMAIN}/${DOMAIN}/g" \
    -e "s/{PANDA_PROFILE}/${PANDA_AWS_PROFILE}/g" \
    -e "s/{AWS_PROFILE}/${MEDIA_AWS_PROFILE}/g" \
    -e "s/{LIVE_STREAM_NAME}/${LIVE_STREAM_NAME}/g" \
    -e "s/{PREVIEW_STREAM_NAME}/${PREVIEW_STREAM_NAME}/g" \
    -e "s/{PREVIEW_REINDEX_STREAM_NAME}/${PREVIEW_REINDEX_STREAM_NAME}/g" \
    -e "s/{PUBLISHED_REINDEX_STREAM_NAME}/${PUBLISHED_REINDEX_STREAM_NAME}/g" \
    -e "s/{DYNAMO_TABLE}/${DYNAMO_TABLE}/g" \
    -e "s/{DYNAMO_PUBLISHED_TABLE}/${DYNAMO_PUBLISHED_TABLE}/g" \
    -e "s/{DYNAMO_AUDIT_TABLE}/${DYNAMO_AUDIT_TABLE}/g" \
    ../conf/reference.conf > ../conf/application.conf

aws s3 cp s3://atom-maker-conf/youtube-DEV.conf ../conf/youtube-DEV.conf --profile $MEDIA_AWS_PROFILE
aws s3 cp s3://atom-maker-conf/capi-DEV.conf ../conf/capi-DEV.conf --profile $MEDIA_AWS_PROFILE
aws s3 cp s3://atom-maker-conf/flexible-DEV.conf ../conf/flexible-DEV.conf --profile $MEDIA_AWS_PROFILE
aws s3 cp s3://atom-maker-conf/kinesis-DEV.conf ../conf/kinesis-DEV.conf --profile $MEDIA_AWS_PROFILE
aws s3 cp s3://atom-maker-conf/sentry-DEV.conf ../conf/sentry-DEV.conf --profile $MEDIA_AWS_PROFILE

