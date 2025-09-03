import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { frontPageSize } from '../constants/frontPageSize';
import { showError } from './error';
import VideosApi, { MediaAtomSummary } from '../services/VideosApi';

type VideoState = {
  entries: MediaAtomSummary[];
  total: number;
  limit: number;
  shouldUseCreatedDateForSort: boolean;
};

const initialState: VideoState = {
  entries: [],
  total: 0,
  limit: frontPageSize,
  shouldUseCreatedDateForSort: false
};

export const fetchVideos = createAsyncThunk<
  { total: number; atoms: MediaAtomSummary[] },
  { search: string; limit: number; shouldUseCreatedDateForSort: boolean }
>(
  'videos/fetchVideos',
  ({ search, limit, shouldUseCreatedDateForSort }, { dispatch }) =>
    VideosApi.fetchVideos(search, limit, shouldUseCreatedDateForSort).catch(
      (error: unknown) => {
        dispatch(showError('Could not get videos', error));
        throw error;
      }
    )
);

const videos = createSlice({
  name: 'videos',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder
      .addCase(fetchVideos.pending, (state, action) => {
        state.limit = action.meta.arg.limit;
        state.shouldUseCreatedDateForSort =
          action.meta.arg.shouldUseCreatedDateForSort;
      })
      .addCase(fetchVideos.fulfilled, (state, action) => {
        state.total = action.payload.total;
        state.entries = action.payload.atoms;
      });
  }
});

export default videos.reducer;
