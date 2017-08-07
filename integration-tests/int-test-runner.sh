#!/usr/bin/env bash

# Only use this runner when wanting to alert to Slack
./sbt integrationTests/test
export STATUS=$?

if [[ $STATUS = 1 ]]
then
  if [ "$INT_TEST_TARGET" = "PROD" ]
  then
    curl -X POST --data-urlencode 'payload={"text": "<!here> Media Atom Maker integration tests have failed on PRODUCTION \nTEAMCITY - '${BUILD_URL}' \nLOGS - '${LOGS_URL}'"}' ${SLACK_URL}
  else
    curl -X POST --data-urlencode 'payload={"text": "Media Atom Maker integration tests have failed on CODE \nTEAMCITY - '${BUILD_URL}' \nLOGS - '${LOGS_URL}'"}' ${SLACK_URL}
  fi
fi

exit $STATUS
