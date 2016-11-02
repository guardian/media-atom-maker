import { combineReducers } from 'redux';
import asset from './assetReducer';
import error from './errorReducer';
import video from './videoReducer';
import videos from './videosReducer';
import searchTerm from './searchTermReducer';
import { reducer as form } from 'redux-form';


export default combineReducers({
  asset,
  error,
  video,
  videos,
  searchTerm,
  form
});
