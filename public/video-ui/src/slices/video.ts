import { Video } from '../services/VideosApi';
import { blankVideoData } from '../constants/blankVideoData';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface SaveVideoState {
  saving: boolean;
  publishing: boolean;
  addingAsset: boolean;
  activatingAssetNumber?: number;
}

interface VideoState {
  video: Video;
  publishedVideo: Video;
  saveVideoState?: SaveVideoState;
}

const initialState: VideoState = {
  video: blankVideoData,
  publishedVideo: null,
  saveVideoState: {
    saving: false,
    publishing: false,
    addingAsset: false
  }
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
    setVideoSaveState: (state, { payload }: PayloadAction<boolean>) => {
      state.saveVideoState = {
        ...state.saveVideoState, saving: payload
      };
    },
    setVideoPublishingState: (state, { payload }: PayloadAction<boolean>) => {
      state.saveVideoState = {
        ...state.saveVideoState, publishing: payload
      };
    },
    setAddingAssetState: (state, { payload }: PayloadAction<boolean>) => {
      state.saveVideoState = {
        ...state.saveVideoState, addingAsset: payload
      };
    },
    setActivatingAssetNumber: (state, { payload }:PayloadAction<number | undefined>) => {
      state.saveVideoState = {
        ...state.saveVideoState,
        saving : payload !== undefined,
        activatingAssetNumber : payload
      };
    }
  },
  selectors: {
    selectVideo: ({ video }) => video,
    selectPublishedVideo: ({ publishedVideo }) => publishedVideo
  },
  extraReducers: builder => {
    builder.addCase('SHOW_ERROR', state => {
      state.saveVideoState.saving = false;
      state.saveVideoState.publishing = false;
      state.saveVideoState.addingAsset = false;
      state.saveVideoState.activatingAssetNumber = undefined;
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
  setVideoSaveState,
  setVideoPublishingState,
  setAddingAssetState,
  setActivatingAssetNumber
} = video.actions;

export const { selectVideo, selectPublishedVideo } = video.selectors;
