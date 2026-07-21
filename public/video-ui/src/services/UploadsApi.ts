import { apiRequest } from './apiRequest';
import { errorDetails } from '../util/errorDetails';
import { S3 } from '@aws-sdk/client-s3';
import { XhrHttpHandler } from '@aws-sdk/xhr-http-handler';
import { Upload } from '../slices/s3Upload';

// TO DO - convert to typescript, use definition of `Upload` at public/video-ui/src/components/VideoUpload/VideoAsset.tsx

/**
 *
 * @param atomId {string}
 * @returns {Promise<unknown>}
 */
export function getUploads(atomId: string) {
  return apiRequest({
    url: `/api/uploads?atomId=${atomId}`
  });
}

/**
 *
 * @param atomId {string}
 * @param file {File}
 * @param selfHost {boolean}
 * @returns {Promise<unknown>}
 */
export function createUpload(atomId: string, file: File, selfHost: boolean) {
  return apiRequest({
    url: `/api/uploads`,
    method: 'post',
    headers: {
      'Csrf-Token': (window as any).guardian.csrf.token
    },
    data: {
      atomId: atomId,
      filename: file.name,
      size: file.size,
      selfHost: selfHost
    }
  });
}

function getCredentials(id: any, key: any) {
  return apiRequest({
    url: `/api/uploads/${id}/credentials?key=${key}`,
    method: 'post',
    headers: {
      'Csrf-Token': (window as any).guardian.csrf.token
    }
  });
}

/**
 *
 * @param region {string}
 * @param credentials {any}
 * @returns {S3}
 */
function getS3(
  region: any,
  credentials: {
    temporaryAccessId: any;
    temporarySecretKey: any;
    sessionToken: any;
  }
) {
  const { temporaryAccessId, temporarySecretKey, sessionToken } = credentials;

  const awsCredentials = {
    accessKeyId: temporaryAccessId,
    secretAccessKey: temporarySecretKey,
    sessionToken: sessionToken
  };

  return new S3({
    credentials: awsCredentials,
    requestHandler: XhrHttpHandler.create({
      requestTimeout: 240_000
    }),
    region: region,
    useAccelerateEndpoint: true
  });
}

/**
 * Upload single part of file
 *
 * @param upload {Upload}
 * @param part {typeof Upload['parts'][number]}
 * @param file {File}
 * @param progressFn {(completed: number) => any}
 * @returns {Promise<unknown>}
 */
function uploadPart(
  upload: { id: any; metadata: { region: any; bucket: any } },
  part: { start: any; end: any; key: any },
  file: { slice: (arg0: any, arg1: any) => any; name: any },
  progressFn: (arg0: any) => void
) {
  const slice = file.slice(part.start, part.end);

  return getCredentials(upload.id, part.key).then(credentials => {
    // @ts-expect-error TS(2345): Argument of type 'unknown' is not assignable to pa... Remove this comment to see the full error message
    const s3 = getS3(upload.metadata.region, credentials);

    const request = slice.arrayBuffer().then((body: any) =>
      s3.putObject({
        Bucket: upload.metadata.bucket,
        Key: part.key,
        Metadata: { original: file.name },
        Body: body
      })
    );

    request.then(() => {
      progressFn(part.end);
    });

    return request;
  });
}

/**
 * Recursively upload all parts of file
 *
 * @param upload {Upload}
 * @param parts {typeof Upload['parts']}
 * @param file {File}
 * @param progressFn {(completed: number) => any}
 * @returns {Promise<boolean>}
 */
export function uploadParts(
  upload: Upload,
  parts: { end: number; key: string; start: number }[],
  file: File,
  progressFn: (completed: number) => {
    payload: number;
    type: 's3Upload/s3UploadProgress';
  }
) {
  return new Promise((resolve, reject) => {
    function uploadPartRecursive(parts: string | any[]) {
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

/**
 * uploads a text file containing subtitles to the given version of the video atom
 * @param id - atom id
 * @param version - video asset version to associate the subtitles with
 * @param file - the local file to upload
 * @returns {Promise}
 */
export function uploadSubtitleFile({
  id,
  version,
  file
}: {
  id: string;
  version: string;
  file: File;
}) {
  const formData = new FormData();
  formData.append('subtitle-file', file);

  return apiRequest({
    url: `/api/uploads/${id}/${version}/subtitle-file`,
    method: 'post',
    headers: {
      'Csrf-Token': (window as any).guardian.csrf.token
    },
    // @ts-expect-error TS(2322): Type 'FormData' is not assignable to type 'string'... Remove this comment to see the full error message
    body: formData,
    processData: false
  });
}

export function deleteSubtitleFile({
  id,
  version
}: {
  id: string;
  version: string;
}) {
  return apiRequest({
    url: `/api/uploads/${id}/${version}/subtitle-file`,
    method: 'delete',
    headers: {
      'Csrf-Token': (window as any).guardian.csrf.token
    },
    // @ts-expect-error TS(2353): Object literal may only specify known properties, ... Remove this comment to see the full error message
    processData: false
  });
}
