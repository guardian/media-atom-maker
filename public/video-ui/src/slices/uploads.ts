import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Upload } from './s3Upload';
import * as UploadsApi from '../services/UploadsApi';
import { showError } from './error';
import { errorDetails } from '../util/errorDetails';

export const getUploads = createAsyncThunk<Upload[], string>(
  'uploads/getUploads',
  (atomId, { dispatch }) =>
    (UploadsApi.getUploads(atomId) as Promise<Upload[]>).catch(err => {
      dispatch(showError(errorDetails(err), err));
      throw err;
    })
);

const initialState: Upload[] = [];

const uploads = createSlice({
  name: 'uploads',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder.addCase(getUploads.fulfilled, (state, { payload }) => payload);
  }
});

export default uploads.reducer;
