import {pandaReqwest} from './pandaReqwest';

// See http://andrewhfarmer.com/aws-sdk-with-webpack/ for why this is strange
import 'aws-sdk/dist/aws-sdk';
const AWS = window.AWS;

class UploadsApi {
  getUploadPolicy(atomId) {
    return pandaReqwest({
      url: `/api2/atom/${atomId}/uploads`,
      method: 'post'
    });
  }

  getS3(config) {
    const { temporaryAccessKey, temporarySecretKey, sessionToken } = config.credentials;
    const credentials = new AWS.Credentials(temporaryAccessKey, temporarySecretKey, sessionToken);

    return new AWS.S3({
      apiVersion: '2006-03-01',
      credentials: credentials,
      params: { Bucket: config.bucket },
      region: config.region
    });
  }

  startUpload(config, file, progress) {
    const s3 = this.getS3(config);

    const params = { Key: config.key, Body: file, ACL: 'private', Metadata: { original: file.name} };
    const request = s3.putObject(params);

    request.on('httpUploadProgress', (event) => {
      progress(event.loaded, event.total);
    });

    return request.promise();
  }
}

export default new UploadsApi();
