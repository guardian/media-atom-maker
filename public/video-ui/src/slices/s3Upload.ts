import { createSlice,PayloadAction } from '@reduxjs/toolkit';

export type YouTubeAsset = { id: string, sources?: undefined };
export type SelfHostedAsset = { id?: undefined, sources: unknown[] }
export type Upload = {
  id: string,
  asset?: YouTubeAsset | SelfHostedAsset;
  processing?: {
    status: string,
    failed: boolean,
    current?: number,
    total?: number
  };
  parts?: { end: number }[];
  metadata?: {
    originalFilename?: string;
    subtitleFilename?: string;
    startTimestamp?: number;
    user: string
  }
};

export interface S3UploadState {
  id: string | null;
  progress: number;
  total: number;
  status: 'idle' | 'uploading' | 'post-processing' | 'complete' | 'error';
}

const initialState : S3UploadState =  { id: null, progress: 0, total: 0, status: 'idle' };

const s3Upload = createSlice({
  name: 's3Upload',
  initialState,
  reducers: {
    s3UploadStarted:(state , action: PayloadAction<Upload>)=> {
      const total = action.payload.parts[action.payload.parts.length - 1].end;
      Object.assign(state, {
        id: action.payload.id,
        total: total,
        status: 'uploading'
      });
    },
    s3UploadProgress: (state , action: PayloadAction<number>)=>{
        state.progress = action.payload,
        state.status =  'uploading'
      },
      setS3UploadStatusToPostProcessing: (state) => {
        state.status = 'post-processing';
      },
      setS3UploadStatusToComplete: (state) => {
        state.status = 'complete';
      },
      setS3UploadStatusToError: (state) => {
        state.status = 'error';
      },
      setS3UploadStatusToReset: _=> ({
       ... initialState
      })
  }
});

export default s3Upload.reducer;

export const {  s3UploadStarted,s3UploadProgress,setS3UploadStatusToPostProcessing,setS3UploadStatusToComplete, setS3UploadStatusToError , setS3UploadStatusToReset} = s3Upload.actions;

