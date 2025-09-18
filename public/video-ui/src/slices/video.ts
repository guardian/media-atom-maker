import { Video } from '../services/VideosApi';
import { blankVideoData } from '../constants/blankVideoData';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface VideoState {
  video: Video;
  publishedVideo?: Video;
  isSaving: boolean;
}

const initialState: VideoState = {
  video: blankVideoData,
  isSaving: false,
};

const video = createSlice({
  name: 'video',
  initialState,
  reducers: {
    setVideo: (state, { payload }: PayloadAction<Video>) => {
      state.video = ({...blankVideoData, ...payload})
    },
    setVideoBlank: state => {

        state.video = {
      ...blankVideoData,
        type: 'media'
      }
    },
    setActiveAsset: (state, { payload }: PayloadAction<Video>) => {
      state.video = {
      ...(state.video || blankVideoData),
      activeVersion: payload.activeVersion
     }
    },
    setAssets: (state, { payload }: PayloadAction<Video>) => {
      state.video = {...(state.video || blankVideoData),
        assets: payload.assets
      }
    }
  },
  selectors: {
    selectVideo: ({video}) => video
  }
});

export default video.reducer;

export const { setVideo, setVideoBlank, setActiveAsset, setAssets } =
  video.actions;

export const {selectVideo} = video.selectors;
