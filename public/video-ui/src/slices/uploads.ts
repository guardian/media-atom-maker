import { createSlice,  PayloadAction } from '@reduxjs/toolkit';
import {Upload} from "./s3Upload";

const initialState:Upload[] = [];
const uploads = createSlice({
  name: 'uploads',
  initialState,
  reducers: {
    setUploads:(_, action: PayloadAction<Upload[]>)=>{
       return  action.payload;
    }
  }
});

export default uploads.reducer;

export const { setUploads } = uploads.actions;
