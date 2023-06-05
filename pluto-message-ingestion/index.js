#!/usr/bin/env node

const KinesisMessageProcessor = require('./kinesis-message-processor');

exports.handler = (event, context, callback) => {
  const kinesisMessageProcessor = new KinesisMessageProcessor();

  kinesisMessageProcessor
    .open()
    .then(() => {
      // eslint-disable-next-line no-console
      console.log({ records: event.Records, count: event.Records.length });
      event.Records.forEach(record => {
        // Kinesis data is base64 encoded so decode here
        const payload = new Buffer(record.kinesis.data, 'base64').toString(
          'utf8'
        );
        // eslint-disable-next-line no-console
        console.log({ record });
        // eslint-disable-next-line no-console
        console.log({ payload });
        try {
          const jsonPayload = JSON.parse(payload);
          kinesisMessageProcessor.process(jsonPayload);
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error(
            `Failed to parse kinesis message as JSON: ${payload}. Removing from the stream.`
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
          // eslint-disable-next-line no-console
          console.log(err);
          callback(`Error. ${err}`);
        });
    })
    .catch(err => {
      callback(err);
    });
};
