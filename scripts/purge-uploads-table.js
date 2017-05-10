#!/usr/bin/env node

const fs = require('fs');
const AWS = require('aws-sdk');
const parseHocon = require('hoconjs/build/hoconjs');

const configPath = '/etc/gu/media-atom-maker.private.conf';

AWS.config.update({
  credentials: new AWS.SharedIniFileCredentials({
    profile: 'media-service'
  }),
  region: 'eu-west-1'
});

function getConfig() {
  return new Promise((resolve, reject) => {
    fs.readFile(configPath, 'utf8', (err, rawFile) => {
      if (err) {
        reject(err);
      } else {
        const config = parseHocon(rawFile);
        resolve(config);
      }
    })
  });
}

function scanTable (tableName) {
  return new Promise((resolve, reject) => {
    const dynamodb = new AWS.DynamoDB();

    dynamodb.scan({TableName: tableName}, (err, data) => {
      if (err) {
        reject(err);
      } else {
        resolve(data);
      }
    });
  })
}

getConfig().then(config => {
  const table = config.aws.dynamo.uploadTrackingTableName;

  const dynamodb = new AWS.DynamoDB();

  scanTable(table).then(data => {
    console.log(`deleting ${data.Items.length} items`);

    data.Items.forEach(item => {
      console.log(item);

      const options = { TableName: table, Key: item };

      dynamodb.deleteItem(options, (err, data) => {
        if (err) {
          console.log(`Error! ${err}`);
        } else {
          console.log('Deleted');
          console.log(data);
        }
      });
    });

    console.log('Done.');
  }).catch(err => {
    console.log(`Error! ${err}`);
  });
});


