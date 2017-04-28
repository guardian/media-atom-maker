const AWS = require('aws-sdk');
const parseHocon = require('hoconjs/build/hoconjs');

const EnvironmentConfig = require('./environment-config');

const FILE_NAME = 'media-atom-maker.private.conf';

class FileConfig {
  static read() {
    return new Promise((resolve, reject) => {
      const s3 = new AWS.S3();

      const params = {
        Bucket: EnvironmentConfig.bucket,
        Key: `${EnvironmentConfig.stage}/${FILE_NAME}`
      };

      s3.getObject(params, (err, data) => {
        if (err) {
          reject(err);
        } else {
          const fileContent = data.Body.toString('utf8');
          const config = parseHocon(fileContent);
          resolve(config);
        }
      });
    });
  }
}

module.exports = FileConfig;
