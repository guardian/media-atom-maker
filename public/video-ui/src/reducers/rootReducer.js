import { combineReducers } from 'redux';
import audits from './auditReducer';
import config from './configReducer';
import error from './errorReducer';
import video from './videoReducer';
import publishedVideo from './publishedVideoReducer';
import videos from './videosReducer';
import saveState from './saveStateReducer';
import searchTerm from './searchTermReducer';
import youtube from './youtubeReducer';
import { reducer as form } from 'redux-form';
import usage from './usageReducer';
import pageCreate from './composerPageReducer';
import upload from './uploadsReducer';
import editState from './editStateReducer';

export default combineReducers({
  audits,
  config,
  error,
  video,
  videos,
  saveState,
  searchTerm,
  youtube,
  form,
  usage,
  pageCreate,
  publishedVideo,
  upload,
  editState
});
