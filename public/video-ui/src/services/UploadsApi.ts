import {apiRequest} from './apiRequest';
import {errorDetails} from '../util/errorDetails';
import {S3, type S3ClientConfig} from '@aws-sdk/client-s3';
import { XhrHttpHandler } from "@aws-sdk/xhr-http-handler";
import type { ClientAsset, Upload} from "../slices/s3Upload";

export function getUploads(atomId: string): Promise<ClientAsset[]> {
  return apiRequest({
    url: `/api/uploads?atomId=${atomId}`
  });
}

export function createUpload(atomId: string, file: File, selfHost: boolean): Promise<Upload> {
  return apiRequest({
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

type TempCredentials = {
  temporaryAccessId: string;
  temporarySecretKey: string;
  sessionToken: string;
}
function getCredentials(id: string, key: string): Promise<TempCredentials> {
  return apiRequest({
    url: `/api/uploads/${id}/credentials?key=${key}`,
    method: 'post',
    headers: {
      'Csrf-Token': window.guardian.csrf.token
    }
  });
}

function getS3(region: string, credentials: any): S3 {
  const { temporaryAccessId, temporarySecretKey, sessionToken } = credentials;

  const awsCredentials: S3ClientConfig['credentials'] = {
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

function uploadPart(upload: Upload, part: Upload['parts'][number], file: File, progressFn: (completed: number) => any): Promise<unknown> {
  const slice = file.slice(part.start, part.end);

  return getCredentials(upload.id, part.key).then(credentials => {
    const s3 = getS3(
      upload.metadata.region,
      credentials
    );

    const request = slice.arrayBuffer().then(body => s3.putObject({
      Bucket: upload.metadata.bucket,
      Key: part.key,
      Metadata: { original: file.name },
      Body: body as Uint8Array // okay yeah this conversion is evil, but it works and sidesteps a bunch of bugs around the sdk's handling of Blob and ReadableStreams....
    }));

    request.then(() => {
      progressFn(part.end);
    });

    return request;
  });
}

export function uploadParts(
  upload: Upload,
  parts: Upload['parts'],
  file: File,
  progressFn: (completed: number) => any
): Promise<boolean> {
  return new Promise((resolve, reject) => {
    function uploadPartRecursive(parts: Upload['parts']) {
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
export function uploadSubtitleFile(
  { id, version, file }: { id: string, version: string; file: File; }
): Promise<Upload> {
  const formData = new FormData();
  formData.append('subtitle-file', file);

  return apiRequest({
    url: `/api/uploads/${id}/${version}/subtitle-file`,
    method: 'post',
    headers: {
      'Csrf-Token': window.guardian.csrf.token
    },
    body: formData
  });
}

export function deleteSubtitleFile(
  { id, version }: { id: string; version: string; }
): Promise<Upload> {
  return apiRequest({
    url: `/api/uploads/${id}/${version}/subtitle-file`,
    method: 'delete',
    headers: {
      'Csrf-Token': window.guardian.csrf.token
    }
  });
}
