import {createSlice} from '@reduxjs/toolkit';

const initialState: { categories: unknown[], channels: unknown[] } = { categories: [], channels: [] };

const youtubeSlice = createSlice({
  name: 'youtube',
  initialState,
  reducers: {
    categoriesReceived: (state, action) => {
      state.categories = action.payload;
    },
    channelsReceived: (state, action) => {
      state.channels = action.payload;
    }
  }
});

export const { categoriesReceived, channelsReceived } = youtubeSlice.actions;

export default youtubeSlice.reducer;
