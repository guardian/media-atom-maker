import { combineReducers } from 'redux';
import error from './errorReducer';
import video from './videoReducer';
import videos from './videosReducer';


export default combineReducers({
  error,
  video,
  videos
});
