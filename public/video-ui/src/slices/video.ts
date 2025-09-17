import { Video } from '../services/VideosApi';
import { blankVideoData } from '../constants/blankVideoData';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

const initialState: Video = blankVideoData;

const video = createSlice({
  name: 'video',
  initialState,
  reducers: {
    setVideo: (_, { payload }: PayloadAction<Video>) =>
      ({...blankVideoData, ...payload}),
    setVideoBlank: _ => ({
      ...blankVideoData,
      type: 'media'
    }),
    setActiveAsset: (state, { payload }: PayloadAction<Video>) => ({
      ...(state || blankVideoData),
      activeVersion: payload.activeVersion
    }),
    setAssets: (state, { payload }: PayloadAction<Video>) => ({
      ...(state || blankVideoData),
      assets: payload.assets
    })
  }
});

export default video.reducer;

export const { setVideo, setVideoBlank, setActiveAsset, setAssets } =
  video.actions;
