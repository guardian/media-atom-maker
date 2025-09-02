import { config as _config, SharedIniFileCredentials } from 'aws-sdk';
import {
  profile as _profile,
  region as _region,
  app,
  isDev
} from './environment-config';
import { read } from './file-config';
import HMACRequest from './hmac-request';
import PlutoMessageProcessor from './pluto-message-processor';

class KinesisMessageProcessor {
  constructor() {
    if (isDev) {
      _config.update({
        credentials: new SharedIniFileCredentials({
          profile: _profile
        }),
        region: _region
      });
    }

    this._messages = [];
  }

  open() {
    return new Promise((resolve, reject) => {
      read()
        .then(config => {
          this.hmacRequest = new HMACRequest({
            serviceName: app,
            secret: config.secret
          });

          this.plutoMessageProcessor = new PlutoMessageProcessor({
            hostname: `https://${config.host}`,
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

export default KinesisMessageProcessor;
