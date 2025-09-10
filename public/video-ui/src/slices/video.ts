import { Video } from '../services/VideosApi';
import { blankVideoData } from '../constants/blankVideoData';
import { createSlice, Action, PayloadAction } from '@reduxjs/toolkit';

const initialState: null | false | Video = null;

interface VideoAction extends Action {
  video?: Video;
}

const isVideoAction = (action: Action): action is VideoAction => {
  return [
    'VIDEO_GET_RECEIVE',
    'VIDEO_CREATE_RECEIVE',
    'VIDEO_SAVE_REQUEST',
    'VIDEO_SAVE_RECEIVE'
  ].includes(action.type);
};

interface PublishVideoAction extends Action {
  publishedVideo: Video;
}

const isPublishVideoAction = (action: Action): action is PublishVideoAction => {
  return 'VIDEO_PUBLISH_RECEIVE' === action.type;
};

const video = createSlice({
  name: 'video',
  initialState,
  reducers: {
    setVideo: (_, { payload }: PayloadAction<Video>) => {
      return payload;
    },
    setVideoBlank: (_) => ({
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
  },
  extraReducers: builder => {
    builder
      .addMatcher(isPublishVideoAction, (_, { publishedVideo }) => ({
        ...blankVideoData,
        ...publishedVideo
      }))
      .addMatcher(isVideoAction, (_, { video }) => {
        if (video) {
          return { ...blankVideoData, ...video };
        } else {
          return false;
        }
      });
  }
});

export default video.reducer;

export const { setVideo, setVideoBlank, setActiveAsset, setAssets } = video.actions;
