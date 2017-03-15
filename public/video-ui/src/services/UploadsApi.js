import {pandaReqwest} from './pandaReqwest';

// See http://andrewhfarmer.com/aws-sdk-with-webpack/ for why this is strange
import 'aws-sdk/dist/aws-sdk';
const AWS = window.AWS;

class UploadFunctions {
  createUpload = (atomId, file) => {
    return pandaReqwest({
      url: `/api2/uploads?atomId=${atomId}`,
      method: 'post',
      contentType: 'application/json',
      data: JSON.stringify({ atomId: atomId, filename: file.name, size: file.size })
    });
  };

  stopUpload = (id) => {
    return pandaReqwest({
      url: `/api2/uploads/_stop`,
      method: 'post',
      contentType: 'application/json',
      data: JSON.stringify({ id: id })
    });
  };

  uploadPart = (upload, part, file, progressFn) => {
    const slice = file.slice(part.start, part.end);

    return this.getCredentials(upload.id, part.key).then((credentials) => {
      const s3 = this.getS3(upload.bucket, upload.region, credentials);

      const params = { Key: part.key, Body: slice, ACL: 'private', Metadata: { original: file.name } };
      const request = s3.upload(params);
      
      request.on('httpUploadProgress', (event) => {
        progressFn(part.start + event.loaded);
      });
      
      return request;
    });
  };

  getCredentials = (id, key) => {
    return pandaReqwest({
      url: `/api2/uploads/${id}/credentials`,
      method: 'post',
      headers: {
        'X-Upload-Key': key
      }
    }).then((resp) => {
      return resp;
    });
  };

  getS3 = (bucket, region, credentials) => {
    const { temporaryAccessId, temporarySecretKey, sessionToken } = credentials;
    const awsCredentials = new AWS.Credentials(temporaryAccessId, temporarySecretKey, sessionToken);

    return new AWS.S3({
      apiVersion: '2006-03-01',
      credentials: awsCredentials,
      params: { Bucket: bucket },
      region: region
    });
  };
}

export const UploadsApi = new UploadFunctions();

export class UploadHandle {
  constructor(upload, file, progressFn) {
    this.upload = upload;
    this.file = file;
    this.progressFn = progressFn;

    this.request = null;
  }

  start = () => {
    this.uploadParts(this.upload.parts);
  };

  stop = () => {
    UploadsApi.stopUpload(this.upload.id);

    if(this.request)
      this.request.abort();
  };

  uploadParts = (parts) => {
    if(parts.length > 0) {
      const partRequest = UploadsApi.uploadPart(this.upload, parts[0], this.file, this.progressFn);

      partRequest.then((s3Request) => {
        this.request = s3Request;

        s3Request.promise().then(() => {
          this.uploadParts(parts.slice(1));
        });
      });
    }
  }
}