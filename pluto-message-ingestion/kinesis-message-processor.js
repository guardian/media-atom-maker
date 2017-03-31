const AWS = require('aws-sdk');
const FileConfig = require('./file-config');
const ELKKinesisLogger = require('elk-kinesis-logger');
const EnvironmentConfig = require('./environment-config');
const HMACRequest = require('./hmac-request');
const PlutoMessageProcessor = require('./pluto-message-processor');

class KinesisMessageProcessor {
  constructor() {
    if (EnvironmentConfig.isDev) {
      AWS.config.update({
        credentials: new AWS.SharedIniFileCredentials({
          profile: EnvironmentConfig.profile
        }),
        region: EnvironmentConfig.region
      });
    }

    this._messages = [];
  }

  open() {
    return new Promise((resolve, reject) => {
      FileConfig.read()
        .then(config => {
          this.logger = new ELKKinesisLogger({
            stage: EnvironmentConfig.stage,
            stack: EnvironmentConfig.stack,
            app: EnvironmentConfig.app,
            roleArn: config.aws.kinesis.stsRoleToAssume,
            streamName: config.aws.kinesis.logging
          });

          this.logger
            .open()
            .then(() => {
              this.logger.log('Starting');

              this.hmacRequest = new HMACRequest({
                serviceName: EnvironmentConfig.app,
                secret: config.secret
              });

              this.plutoMessageProcessor = new PlutoMessageProcessor({
                hostname: `https://${config.host}`,
                hmacRequest: this.hmacRequest,
                logger: this.logger
              });

              resolve();
            })
            .catch(err => reject(`Failed to open logger. ${err}`));
        })
        .catch(err => {
          reject(`Failed to read config file. ${err}`);
        });
    });
  }

  close() {
    // eslint-disable-next-line no-console
    console.log(`Ensuring ${this._messages.length} messages are processed`);

    return new Promise((resolve, reject) => {
      Promise.all(this._messages)
        .then(() => {
          this.logger.log('Finished successfully');
          this.logger.close().then(() => resolve('done'));
        })
        .catch(err => {
          this.logger.log('Finished with errors');
          this.logger.close().then(() => reject(err));
        });
    });
  }

  process(message) {
    this._messages.push(this.plutoMessageProcessor.process(message));
  }
}

module.exports = KinesisMessageProcessor;
