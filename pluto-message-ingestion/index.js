#!/usr/bin/env node

const KinesisMessageProcessor = require('./kinesis-message-processor');
const logForElk = require('./logger');

exports.handler = (event, context, callback) => {
  const kinesisMessageProcessor = new KinesisMessageProcessor();

  kinesisMessageProcessor
    .open()
    .then(() => {
      event.Records.forEach(record => {
        // Kinesis data is base64 encoded so decode here
        const payload = new Buffer(record.kinesis.data, 'base64').toString(
          'utf8'
        );
        try {
          const jsonPayload = JSON.parse(payload);
          kinesisMessageProcessor.process(jsonPayload);
        } catch (e) {
          logForElk(
            {
              message: `Failed to parse kinesis message as JSON: ${payload}. Removing from the stream.`
            },
            'error'
          );
          callback(null, 'Message removed from stream due to invalid data.');
        }
      });

      kinesisMessageProcessor
        .close()
        .then(() => {
          callback(null, 'Done');
        })
        .catch(err => {
          logForElk({ message: err }, 'error');
          callback(`Error. ${err}`);
        });
    })
    .catch(err => {
      callback(err);
    });
};
