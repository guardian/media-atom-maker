import { createSlice,PayloadAction } from '@reduxjs/toolkit';

export interface S3UploadState {
  id: string | null;
  progress: number;
  total: number;
  status: 'idle' | 'uploading' | 'post-processing' | 'complete' | 'error';
}

type S3UploadAction = PayloadAction<S3UploadState>

const initialState : S3UploadState =  { id: null, progress: 0, total: 0, status: 'idle' };

const s3Upload = createSlice({
  name: 's3Upload',
  initialState,
  reducers: {
    s3UploadStarted:(state :S3UploadState, action)=> {
      const total = action.payload.parts[action.payload.parts.length - 1].end;
      Object.assign(state, {
        id: action.payload.id,
        total: total,
        status: 'uploading'
      });
    },
    s3UploadProgress: (state : S3UploadState, action: S3UploadAction)=>{
      Object.assign(state, {
        progress: action.payload.progress,
        status: 'uploading'
      });
      },
      s3UploadPostProcessing: _=> ({
        ...initialState, status: 'post-processing'
      }),
      s3UploadComplete: _=> ({
        ...initialState, status: 'complete'
      }),
      s3UploadShowError: _=> ({
        ...initialState, status: 'error'
      }),
      s3UploadReset: _=> ({
       ... initialState
      })
  }
});

export default s3Upload.reducer;

export const {  s3UploadStarted,s3UploadProgress,s3UploadPostProcessing,s3UploadComplete, s3UploadShowError , s3UploadReset} = s3Upload.actions;

