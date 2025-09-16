import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { frontPageSize } from '../constants/frontPageSize';
import { showError } from './error';
import VideosApi, { MediaAtomSummary } from '../services/VideosApi';

type VideoState = {
  entries: MediaAtomSummary[];
  total: number;
  limit: number;
};

const initialState: VideoState = {
  entries: [],
  total: 0,
  limit: frontPageSize
};

export const fetchVideos = createAsyncThunk<
  { total: number; atoms: MediaAtomSummary[] },
  { search: string; limit: number; shouldUseCreatedDateForSort: boolean; mediaPlatformFilter: string; }
>(
  'videos/fetchVideos',
  ({ search, limit, shouldUseCreatedDateForSort, mediaPlatformFilter }, { dispatch }) =>
    VideosApi.fetchVideos(search, limit, shouldUseCreatedDateForSort, mediaPlatformFilter).catch(
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
      })
      .addCase(fetchVideos.fulfilled, (state, action) => {
        state.total = action.payload.total;
        state.entries = action.payload.atoms;
      });
  }
});

export default videos.reducer;
