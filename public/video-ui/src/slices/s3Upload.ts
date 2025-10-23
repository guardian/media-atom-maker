import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { getVideo } from '../actions/VideoActions/getVideo';
import {
  createUpload,
  deleteSubtitleFile,
  uploadParts,
  uploadSubtitleFile
} from '../services/UploadsApi';
import { errorDetails } from '../util/errorDetails';
import { showError } from './error';
import { getUploads } from './uploads';

export type YouTubeAsset = { id: string; sources?: undefined };
export type SelfHostedAsset = {
  id?: undefined;
  sources: { src?: string; mimeType?: string }[];
};
export type Upload = {
  id: string;
  asset?: YouTubeAsset | SelfHostedAsset;
  processing?: {
    status: string;
    failed: boolean;
    current?: number;
    total?: number;
  };
  parts?: { end: number; key: string; start: number }[];
  metadata?: {
    originalFilename?: string;
    subtitleFilename?: string;
    startTimestamp?: number;
    user: string;
  };
};

export interface S3UploadState {
  id: string | null;
  progress: number;
  total: number;
  status: 'idle' | 'uploading' | 'complete' | 'error';
}

export const startVideoUpload = createAsyncThunk<
  unknown,
  { id: string; file: File; selfHost?: boolean }
>('s3Upload/startVideoUpload', ({ id, file, selfHost }, { dispatch }) => {
  return createUpload(id, file, selfHost)
    .then((upload: Upload) => {
      dispatch(s3UploadStarted(upload));

      const progress = (completed: number) =>
        dispatch(s3UploadProgress(completed));

      return uploadParts(upload, upload.parts, file, progress)
        .then(() => {
          dispatch(setS3UploadStatusToComplete());
          dispatch(getUploads(id));
          dispatch(getVideo(id));
        })
        .catch(err => {
          dispatch(showError(errorDetails(err), err));
          dispatch(setS3UploadStatusToError());
        });
    })
    .catch(err => {
      dispatch(showError(errorDetails(err), err));
      dispatch(setS3UploadStatusToError());
    });
});

export const startSubtitleFileUpload = createAsyncThunk<
  unknown,
  { id: string; version: string; file: File }
>('s3Upload/startSubtitleFileUpload', (subtitle, { dispatch }) =>
  uploadSubtitleFile(subtitle)
    .then(() => dispatch(setS3UploadStatusToComplete()))
    .catch(err => {
      dispatch(showError(errorDetails(err), err));
      dispatch(setS3UploadStatusToError());
    })
);

export const deleteSubtitle = createAsyncThunk<
  unknown,
  { id: string; version: string }
>('s3Upload/startSubtitleFileUpload', (subtitle, { dispatch }) =>
  deleteSubtitleFile(subtitle)
    .then(() => dispatch(setS3UploadStatusToComplete()))
    .catch(err => {
      dispatch(showError(errorDetails(err), err));
      dispatch(setS3UploadStatusToError());
    })
);

const initialState: S3UploadState = Object.freeze({
  id: null,
  progress: 0,
  total: 0,
  status: 'idle'
});

const s3Upload = createSlice({
  name: 's3Upload',
  initialState,
  reducers: {
    s3UploadStarted: (state, action: PayloadAction<Upload>) => {
      const total = action.payload.parts[action.payload.parts.length - 1].end;
      Object.assign(state, {
        id: action.payload.id,
        total: total,
        status: 'uploading'
      });
    },
    s3UploadProgress: (state, action: PayloadAction<number>) => {
      (state.progress = action.payload), (state.status = 'uploading');
    },
    setS3UploadStatusToComplete: state => {
      state = {
        ...initialState
      };
    },
    setS3UploadStatusToError: state => {
      state.status = 'error';
    },
    resetS3UploadState: () => ({
      ...initialState
    })
  }
});

export default s3Upload.reducer;

export const {
  s3UploadStarted,
  s3UploadProgress,
  setS3UploadStatusToComplete,
  setS3UploadStatusToError,
  resetS3UploadState
} = s3Upload.actions;
