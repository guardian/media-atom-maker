import { pandaReqwest } from './pandaReqwest';
import { errorDetails } from '../util/errorDetails';

// See http://andrewhfarmer.com/aws-sdk-with-webpack/ for why this is strange
import 'aws-sdk/dist/aws-sdk';
const AWS = window.AWS;

// The timeout for individual upload requests. Defaults to 120s. This
// default causes problems on slow connections – for example, w/ a 0.5mbps
// upload speed (3.75MB/minute), uploads for 8mb chunks will never complete.
AWS.config.httpOptions.timeout = 240_000; // in ms
const httpOptions = {
  // The number of multipart uploads to run concurrently. Defaults to 4,
  // which has caused problems with slow connections timing out requests
  // prematurely. We judge allowing uploads on slow connections to be
  // more valuable than a minor boost in upload speed due to concurrent uploads.
  queueSize: 1
};

export function getUploads(atomId) {
  return pandaReqwest({
    url: `/api/uploads?atomId=${atomId}`
  });
}

export function createUpload(atomId, file, selfHost) {
  return pandaReqwest({
    url: `/api/uploads`,
    method: 'post',
    headers: {
      'Csrf-Token': window.guardian.csrf.token
    },
    data: {
      atomId: atomId,
      filename: file.name,
      size: file.size,
      selfHost: selfHost
    }
  });
}

function getCredentials(id, key) {
  return pandaReqwest({
    url: `/api/uploads/${id}/credentials?key=${key}`,
    method: 'post',
    headers: {
      'Csrf-Token': window.guardian.csrf.token
    }
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
    region: region,
    useAccelerateEndpoint: true
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
    const request = s3.upload(params, httpOptions);

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

export function uploadPacFile({ id, file }) {
  const formData = new FormData();
  formData.append('pac-file', file);

  return pandaReqwest({
    url: `/api/atom/${id}/pac-file`,
    method: 'post',
    headers: {
      'Csrf-Token': window.guardian.csrf.token
    },
    data: formData,
    contentType: 'multipart/form-data',
    processData: false
  });
}

/**
 * uploadSubtitleFile
 *
 * export function uploadSubtitleFile({ id, file })
 * - add new function to be called from s3Uploads.js
 * - based on uploadPacFile()
 * - calling a new endpoint /api/atom/${id}/subtitle-file
 */
