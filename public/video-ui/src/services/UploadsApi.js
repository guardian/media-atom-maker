import {pandaReqwest} from './pandaReqwest';
import {errorDetails} from '../util/errorDetails';

// See http://andrewhfarmer.com/aws-sdk-with-webpack/ for why this is strange
import 'aws-sdk/dist/aws-sdk';
const AWS = window.AWS;

class UploadFunctions {
  getUploads = (atomId) => {
    return pandaReqwest({
      url: `/api2/uploads?atomId=${atomId}`,
      method: 'get'
    });
  };

  createUpload = (atomId, file, selfHost) => {
    return pandaReqwest({
      url: `/api2/uploads?atomId=${atomId}`,
      method: 'post',
      contentType: 'application/json',
      data: {
        atomId: atomId,
        filename: file.name,
        size: file.size,
        selfHost: selfHost,
        syncWithPluto: true
      }
    });
  };

  stopUpload = (id) => {
    return pandaReqwest({
      url: `/api2/uploads/${id}`,
      method: 'delete'
    });
  };

  uploadPart = (upload, part, file, progressFn) => {
    const slice = file.slice(part.start, part.end);

    return this.getCredentials(upload.id, part.key).then((credentials) => {
      const s3 = this.getS3(upload.metadata.bucket, upload.metadata.region, credentials);

      const params = { Key: part.key, Body: slice, ACL: 'private', Metadata: { original: file.name } };
      const request = s3.upload(params);

      request.on('httpUploadProgress', (event) => {
        progressFn(part.start + event.loaded);
      });

      return request;
    });
  };

  completePart = (id, key, uploadUri) => {
    const headers = { 'X-Upload-Key': key };

    if(uploadUri) {
      headers['X-Upload-Uri'] = uploadUri;
    }

    return pandaReqwest({
      url: `/api2/uploads/${id}/complete`,
      method: 'post',
      headers: headers
    });
  };

  getCredentials = (id, key) => {
    return pandaReqwest({
      url: `/api2/uploads/${id}/credentials`,
      method: 'post',
      headers: {
        'X-Upload-Key': key
      }
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
  constructor(upload, file, progressFn, completeFn, errFn) {
    this.upload = upload;
    this.file = file;
    this.progressFn = progressFn;
    this.completeFn = completeFn;
    this.errFn = errFn;

    this.request = null;
    this.uploadUri = null;
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
      const part = parts[0];

      UploadsApi.uploadPart(this.upload, part, this.file, this.progressFn).then((s3Request) => {
        s3Request.promise().then(() => {
          this.request = s3Request;

          UploadsApi.completePart(this.upload.id, part.key, this.uploadUri).then((resp) => {
            this.uploadUri = resp.uploadUri;
            this.uploadParts(parts.slice(1));
          }).catch((err) => {
            this.errFn(`Error completing part ${part.key}: ${errorDetails(err)}`);
          });
        }).catch((err) => {
          this.errFn(`Error uploading part ${part.key} to S3: ${errorDetails(err)}`);
        });
      }).catch((err) => {
        this.errFn(`Error constructing upload for part ${part.key}: ${errorDetails(err)}`);
      });
    } else {
      this.completeFn();
    }
  }
}
