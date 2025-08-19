import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import {
  getYoutubeCategories,
  getYoutubeChannels,
  YouTubeChannelWithData,
  YouTubeVideoCategory
} from '../services/YoutubeApi';
import Logger from '../logger';

const initialState: {
  categories: YouTubeVideoCategory[];
  channels: YouTubeChannelWithData[];
} = { categories: [], channels: [] };

const errorReceivingCategories = (error: unknown) => {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube categories',
    error: JSON.stringify(error),
    receivedAt: Date.now()
  };
};

export const fetchCategories = createAsyncThunk(
  'youtube/fetchCategories',
  (_, { dispatch }) =>
    getYoutubeCategories().catch(error => {
      dispatch(errorReceivingCategories(error));
      throw error;
    })
);

const errorReceivingChannels = (error: unknown) => {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube channels',
    error: JSON.stringify(error),
    receivedAt: Date.now()
  };
};

export const fetchChannels = createAsyncThunk(
  'youtube/fetchChannels',
  (_, { dispatch }) =>
    getYoutubeChannels().catch(error => {
      dispatch(errorReceivingChannels(error));
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
        state.categories = action.payload;
      })
      .addCase(fetchChannels.fulfilled, (state, action) => {
        state.channels = action.payload;
      });
  }
});

export default youtube.reducer;
