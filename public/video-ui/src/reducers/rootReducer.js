import { combineReducers } from 'redux';
import error from './errorReducer';
import video from './videoReducer';
import videos from './videosReducer';
import searchTerm from './searchTermReducer';


export default combineReducers({
  error,
  video,
  videos,
  searchTerm
});
