import { pandaReqwest } from './pandaReqwest';
import { errorDetails } from '../util/errorDetails';

// See http://andrewhfarmer.com/aws-sdk-with-webpack/ for why this is strange
import 'aws-sdk/dist/aws-sdk';
const AWS = window.AWS;

export function getUploads(atomId) {
  return pandaReqwest({
    url: `/api2/uploads?atomId=${atomId}`
  });
}

export function createUpload(atomId, file, selfHost) {
  return pandaReqwest({
    url: `/api2/uploads?atomId=${atomId}`,
    method: 'post',
    data: {
      atomId: atomId,
      filename: file.name,
      size: file.size,
      selfHost: selfHost,
      syncWithPluto: false // TODO change this once full Pluto integration is complete
    }
  });
}

function getCredentials(id, key) {
  return pandaReqwest({
    url: `/api2/uploads/${id}/credentials?key=${key}`,
    method: 'post'
  });
}

function getS3(bucket, region, credentials) {
  const { temporaryAccessId, temporarySecretKey, sessionToken } = credentials;
  const awsCredentials = new AWS.Credentials(
    temporaryAccessId,
    temporarySecretKey,
    sessionToken
  );

  return new AWS.S3({
    apiVersion: '2006-03-01',
    credentials: awsCredentials,
    params: { Bucket: bucket },
    region: region
  });
}

function uploadPart(upload, part, file, progressFn) {
  const slice = file.slice(part.start, part.end);

  return getCredentials(upload.id, part.key).then(credentials => {
    const s3 = getS3(
      upload.metadata.bucket,
      upload.metadata.region,
      credentials
    );

    const params = {
      Key: part.key,
      Body: slice,
      ACL: 'private',
      Metadata: { original: file.name }
    };
    const request = s3.upload(params);

    request.on('httpUploadProgress', event => {
      progressFn(part.start + event.loaded);
    });

    return request.promise();
  });
}

export function uploadParts(upload, parts, file, progressFn) {
  return new Promise((resolve, reject) => {
    function uploadPartRecursive(parts) {
      if (parts.length === 0) {
        resolve(true);
      } else {
        const part = parts[0];
        const result = uploadPart(upload, part, file, progressFn);

        result
          .then(() => {
            uploadPartRecursive(parts.slice(1));
          })
          .catch(err => {
            reject(errorDetails(err));
          });
      }
    }

    uploadPartRecursive(parts);
  });
}
