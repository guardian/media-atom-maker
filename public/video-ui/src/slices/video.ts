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
    'VIDEO_SAVE_RECEIVE',
  ].includes(action.type);
};

interface PublishVideoAction extends Action {
  publishedVideo: Video;
}

const isPublishVideoAction = (action: Action): action is PublishVideoAction => {
  return 'VIDEO_PUBLISH_RECEIVE' === action.type;
};

interface AssertRevertAction extends VideoAction, Action {}

const isAssertRevertAction = (action: Action): action is AssertRevertAction => {
  return 'ASSET_REVERT_RECEIVE' === action.type;
};

interface AssertCreateAction extends VideoAction, Action {}

const isAssertCreateAction = (action: Action): action is AssertCreateAction => {
  return 'VIDEO_PUBLISH_RECEIVE' === action.type;
};

const video = createSlice({
  name: 'video',
  initialState,
  reducers: {
    setVideo: (state, action: PayloadAction<Video>) => {
      return action.payload;
    }
  },
  extraReducers: builder => {
    builder
      .addCase('VIDEO_POPULATE_BLANK', _ => ({
        ...blankVideoData,
        type: 'media'
      }))
      .addMatcher(isPublishVideoAction, (_, { publishedVideo }) => ({
        ...blankVideoData,
        ...publishedVideo
      }))
      .addMatcher(isAssertRevertAction, (state, { video }) => ({
        ...(state || blankVideoData),
        activeVersion: video.activeVersion
      }))
      .addMatcher(isAssertCreateAction, (state, { video }) => ({
        ...(state || blankVideoData),
        assets: video.assets
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

export const { setVideo } = video.actions;
