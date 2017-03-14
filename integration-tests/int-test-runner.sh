# Only use this runner when wanting to alert to Slack
./sbt integrationTests/test
export STATUS=$?

if [ $STATUS -eq 1 ]
then
  curl -X POST --data-urlencode 'payload={"text": "Media Atom Maker integration tests have failed on CODE"}' ${SLACK_URL}
fi
