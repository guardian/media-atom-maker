import { Video } from '../services/VideosApi';
import { blankVideoData } from '../constants/blankVideoData';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';


interface VideoState {
  video: Video;
  publishedVideo: Video;
  saving: boolean;
  publishing: boolean;
  addingAsset: boolean;
  activatingAssetNumber?: number;
}
const initialState: VideoState = {
  video: blankVideoData,
  publishedVideo: null,
  saving: false,
  publishing: false,
  addingAsset: false
};

const video = createSlice({
  name: 'video',
  initialState,
  reducers: {
    setVideo: (state, { payload }: PayloadAction<Video>) => {
      state.video = { ...blankVideoData, ...payload };
    },
    setPublishedVideo: (state, { payload }: PayloadAction<Video>) => {
      state.publishedVideo = { ...blankVideoData, ...payload };
    },
    setVideoAndPublishedVideo: (state, { payload }: PayloadAction<Video>) => {
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
    },
    setSaving: (state, { payload }: PayloadAction<boolean>) => {
      state.saving = payload;
    },
    setPublishing: (state, { payload }: PayloadAction<boolean>) => {
      state.publishing = payload;
    },
    setAddingAsset: (state, { payload }: PayloadAction<boolean>) => {
      state.addingAsset =  payload;
    },
    setActivatingAssetNumber: (state, { payload }:PayloadAction<number | undefined>) => {
      state.saving =payload !== undefined;
      state.activatingAssetNumber = payload;
    }
  },
  selectors: {
    selectVideo: ({ video }) => video,
    selectPublishedVideo: ({ publishedVideo }) => publishedVideo
  },
  extraReducers: builder => {
    builder.addCase('SHOW_ERROR', state => {
      state.saving = false;
      state.publishing = false;
      state.addingAsset = false;
      state.activatingAssetNumber = undefined;
    });
  }
});

export default video.reducer;

export const {
  setVideo,
  setPublishedVideo,
  setVideoAndPublishedVideo,
  setVideoBlank,
  setActiveAsset,
  setAssets,
  setSaving,
  setPublishing,
  setAddingAsset,
  setActivatingAssetNumber
} = video.actions;

export const { selectVideo, selectPublishedVideo } = video.selectors;
