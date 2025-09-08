const AWS = require('aws-sdk');
const EnvironmentConfig = require('./environment-config');
const HMACRequest = require('./hmac-request');
const PlutoMessageProcessor = require('./pluto-message-processor');
const { readSecretConfig } = require('./secrets-manager');

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
      readSecretConfig()
        .then(({ hmacSecret, host }) => {
          this.hmacRequest = new HMACRequest({
            serviceName: EnvironmentConfig.app,
            secret: hmacSecret
          });

          this.plutoMessageProcessor = new PlutoMessageProcessor({
            hostname: `https://${host}`,
            hmacRequest: this.hmacRequest
          });

          resolve();
        })
        .catch(err => {
          reject(`Failed to read config file. ${err}`);
        });
    });
  }

  close() {
    return new Promise((resolve, reject) => {
      Promise.all(this._messages)
        .then(() => resolve('done'))
        .catch(err => reject(err));
    });
  }

  process(message) {
    this._messages.push(this.plutoMessageProcessor.process(message));
  }
}

module.exports = KinesisMessageProcessor;
