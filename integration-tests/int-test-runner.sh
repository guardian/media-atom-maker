# Only use this runner when wanting to alert to Slack
./sbt integrationTests/test
export STATUS=$?

if [ $STATUS -eq 1 ]
then
  if [ "$INT_TEST_TARGET" == "PROD" ]
  then
    curl -X POST --data-urlencode 'payload={"text": "Media Atom Maker integration tests have failed on PRODUCTION"}' ${SLACK_URL}
  else
    curl -X POST --data-urlencode 'payload={"text": "Media Atom Maker integration tests have failed on CODE"}' ${SLACK_URL}
  fi
fi
exit $STATUS
