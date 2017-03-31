#!/usr/bin/env node

const AWS = require('aws-sdk');

AWS.config.update({
  credentials: new AWS.SharedIniFileCredentials({profile: 'media-service'}),
  region: 'eu-west-1'
});

class KinesisMessenger {
  constructor({streamName}) {
    this.streamName = streamName;

    this.kinesis = new AWS.Kinesis();
  }

  send() {
    return new Promise((resolve, reject) => {
      const message = {
        project_id: 'test',
        collectionId: 'test',
        gnm_project_headline: 'test',
        gnm_project_production_office: 'test',
        gnm_project_status: 'test',
        created: new Date(),
        type: 'project-created'
      };

      const options = {
        StreamName: this.streamName,
        PartitionKey: 'partitionKey-1',
        Data: JSON.stringify(message)
      };

      this.kinesis.putRecord(options, err => {
        if (err) {
          reject(err);
        } else {
          resolve(options);
        }
      });
    });
  }
}

const messenger = new KinesisMessenger({
  streamName: process.env.STREAM_NAME
});

messenger.send().then((msg) => {
  console.log(msg);
  console.log('Done. The lambda should now run...');
});
