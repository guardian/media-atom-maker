import { Video } from '../services/VideosApi';
import { blankVideoData } from '../constants/blankVideoData';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface VideoState {
  video: Video;
  publishedVideo: Video;
}

const initialState: VideoState = {
  video: blankVideoData,
  publishedVideo: null
};

const video = createSlice({
  name: 'video',
  initialState,
  reducers: {
    setVideo: (state, { payload }: PayloadAction<Video>) => {
      state.video = { ...blankVideoData, ...payload };
    },
    setPublishedVideo: (state, { payload }: PayloadAction<Video>) => {
      state.video = { ...blankVideoData, ...payload };
      state.publishedVideo = { ...state.video };
    },
    setVideoBlank: state => {
      state.video = {
        ...blankVideoData,
        type: 'media'
      };
    },
    setActiveAsset: (state, { payload }: PayloadAction<Video>) => {
      state.video = {
        ...(state.video || blankVideoData),
        activeVersion: payload.activeVersion
      };
    },
    setAssets: (state, { payload }: PayloadAction<Video>) => {
      state.video = {
        ...(state.video || blankVideoData),
        assets: payload.assets
      };
    }
  },
  selectors: {
    selectVideo: ({ video }) => video,
    selectPublishedVideo: ({ publishedVideo }) => publishedVideo
  }
});

export default video.reducer;

export const {
  setVideo,
  setPublishedVideo,
  setVideoBlank,
  setActiveAsset,
  setAssets
} = video.actions;

export const { selectVideo, selectPublishedVideo } = video.selectors;
