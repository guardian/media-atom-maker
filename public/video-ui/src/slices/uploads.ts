import { createSlice,  PayloadAction } from '@reduxjs/toolkit';
import _ from "lodash";
import {Upload} from "./s3Upload";

const initialState:Upload[] = [];
const uploads = createSlice({
  name: 'uploads',
  initialState,
  reducers: {
    uploadStarted: (state, action: PayloadAction<Upload>)=> {
      const id = action.payload.id;
      if (!_.find(state, upload => upload.id === id)) {
          state.push({
              ...action.payload,
              processing: { status: 'Uploading', failed: false }
          });
      }
        
    },
    runningUploads:(_, action: PayloadAction<Upload[]>)=>{
       return  action.payload;
    }
  }
});

export default uploads.reducer;

export const { uploadStarted, runningUploads } = uploads.actions;
