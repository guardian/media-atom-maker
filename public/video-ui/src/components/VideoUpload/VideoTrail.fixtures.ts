import { blankVideoData } from '../../constants/blankVideoData';
import { S3UploadState } from '../../slices/s3Upload';

export const s3UploadStarting: S3UploadState = {
  id: 'AAAAAAAAAAA',
  progress: 0,
  total: 100,
  status: 'starting'
};

export const s3UploadUploading: S3UploadState = {
  id: 'AAAAAAAAAAA',
  progress: 50,
  total: 100,
  status: 'uploading'
};

export const s3UploadComplete: S3UploadState = {
  id: 'AAAAAAAAAAA',
  progress: 100,
  total: 100,
  status: 'complete'
};

export const s3UploadError: S3UploadState = {
  id: 'AAAAAAAAAAA',
  progress: 50,
  total: 100,
  status: 'error'
};

export const defaultProps = {
  video: blankVideoData,
  uploads: [],
  setAsset: jest.fn(),
  hasPendingUpload: true,
  s3UploadState: s3UploadStarting
};
