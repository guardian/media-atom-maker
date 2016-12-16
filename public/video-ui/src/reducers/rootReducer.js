import { combineReducers } from 'redux';
import asset from './assetReducer';
import config from './configReducer';
import error from './errorReducer';
import video from './videoReducer';
import videos from './videosReducer';
import saveState from './saveStateReducer';
import searchTerm from './searchTermReducer';
import youtube from './youtubeReducer';
import { reducer as form } from 'redux-form';
import usage from './usageReducer';
import pageCreate from './composerPageReducer';

export default combineReducers({
  asset,
  config,
  error,
  video,
  videos,
  saveState,
  searchTerm,
  youtube,
  form,
  usage,
  pageCreate
});
