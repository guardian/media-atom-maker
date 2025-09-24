import { S3 } from 'aws-sdk';
import parseHocon from 'hocon-parser';

import { bucket, stage } from './environment-config';

const FILE_NAME = 'media-atom-maker.private.conf';

class FileConfig {
  static read() {
    return new Promise((resolve, reject) => {
      const s3 = new S3();

      const params = {
        Bucket: bucket,
        Key: `${stage}/${FILE_NAME}`
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

export default FileConfig;
