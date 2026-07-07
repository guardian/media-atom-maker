import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { CropOption, getCropOptions } from '../services/GridMetadata';
import { showError } from './error';
export type GridState = {
  cropOptions: CropOption[];
};

const initialState: GridState = { cropOptions: [] };
export const fetchCropOptions = createAsyncThunk(
  'grid/fetchCropOptions',
  (_, { dispatch }) =>
    getCropOptions().catch(error => {
      dispatch(showError('Could not get Grid Crop Options', error));
      throw error;
    })
);

const gridMetadata = createSlice({
  name: 'gridMetadata',
  initialState,
  reducers: {},
  extraReducers(builder) {
    builder.addCase(fetchCropOptions.fulfilled, (state, action) => {
      state.cropOptions = action.payload;
    });
  }
});

export default gridMetadata.reducer;
