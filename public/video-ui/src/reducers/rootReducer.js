import { combineReducers } from 'redux';
import config from './configReducer';
import error from './errorReducer';
import video from './videoReducer';
import videos from './videosReducer';
import { reducer as form } from 'redux-form'


export default combineReducers({
  config,
  error,
  video,
  videos,
  form
});
