import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import {
  getYoutubeCategories,
  getYoutubeChannels,
  YouTubeChannelWithData,
  YouTubeVideoCategory
} from '../services/YoutubeApi';
import { showError } from './error';

const initialState: {
  categories: YouTubeVideoCategory[];
  channels: YouTubeChannelWithData[];
} = { categories: [], channels: [] };

export const fetchCategories = createAsyncThunk(
  'youtube/fetchCategories',
  (_, { dispatch }) =>
    getYoutubeCategories().catch(error => {
      dispatch(showError('Could not get YouTube categories', error));
      throw error;
    })
);

export const fetchChannels = createAsyncThunk(
  'youtube/fetchChannels',
  (_, { dispatch }) =>
    getYoutubeChannels().catch(error => {
      dispatch(showError('Could not get YouTube channels', error));
      throw error;
    })
);

const youtube = createSlice({
  name: 'youtube',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder
      .addCase(fetchCategories.fulfilled, (state, action) => {
        if (action.payload.length !== 0 || state.categories.length !== 0) {
          state.categories = action.payload;
        }
      })
      .addCase(fetchChannels.fulfilled, (state, action) => {
        if (action.payload.length !== 0 || state.channels.length !== 0) {
          state.channels = action.payload;
        }
      });
  }
});

export default youtube.reducer;
