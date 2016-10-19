import { combineReducers } from 'redux';
import error from './errorReducer';
import video from './videoReducer';
import videos from './videosReducer';
import { reducer as form } from 'redux-form'


export default combineReducers({
  error,
  video,
  videos,
  form
});
