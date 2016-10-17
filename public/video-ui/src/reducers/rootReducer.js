import { combineReducers } from 'redux';
import video from './videoReducer';
import videos from './videosReducer';


export default combineReducers({
  video,
  videos
});
