import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import {ClientAsset, Upload} from './s3Upload';
import * as UploadsApi from '../services/UploadsApi';
import { showError } from './error';
import { errorDetails } from '../util/errorDetails';

export const getUploads = createAsyncThunk<ClientAsset[], string>(
  'uploads/getUploads',
  (atomId, { dispatch }) =>
    (UploadsApi.getUploads(atomId) as Promise<ClientAsset[]>).catch(err => {
      dispatch(showError(errorDetails(err), err));
      throw err;
    })
);

const initialState: ClientAsset[] = [];

const uploads = createSlice({
  name: 'uploads',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder.addCase(getUploads.fulfilled, (state, { payload }) => payload);
  }
});

export default uploads.reducer;
