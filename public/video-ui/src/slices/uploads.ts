import { createSlice,  PayloadAction } from '@reduxjs/toolkit';
import _ from "lodash";

export type UploadsState = any;

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
  metadata?: {
    originalFilename?: string;
    subtitleFilename?: string;
    startTimestamp?: number;
    user: string
  }
}

const initialState : UploadsState = [];
const uploads = createSlice({
  name: 'uploads',
  initialState,
  reducers: {
    uploadStarted(state: UploadsState, action: PayloadAction<Upload>) {
      const id = action.payload.id;
      if (!_.find(state, upload => upload.id === id)) {
        const status = {
          id,
          failed: false,
          processing: { status: 'Uploading' }
        };
        return [status, ...state];
      }
      return state;
    },
    runningUploads:(_, action: PayloadAction<Upload>)=>{
       return  action.payload;
    }
  }
});

export default uploads.reducer;

export const { uploadStarted,runningUploads } = uploads.actions;
