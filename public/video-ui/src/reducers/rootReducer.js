import { combineReducers } from 'redux';
import { routerReducer } from 'react-router-redux';
import audits from './auditReducer';
import config from './configReducer';
import error from './errorReducer';
import video from './videoReducer';
import publishedVideo from './publishedVideoReducer';
import videos from './videosReducer';
import saveState from './saveStateReducer';
import searchTerm from './searchTermReducer';
import youtube from './youtubeReducer';
import usage from './usageReducer';
import pageCreate from './composerPageReducer';
import s3Upload from './s3UploadReducer';
import editState from './editStateReducer';
import plutoVideos from './plutoVideosReducer';
import checkedFormFields from './checkedFormFieldsReducer';
import uploads from './uploadsReducer';

export default combineReducers({
  audits,
  config,
  error,
  video,
  videos,
  saveState,
  searchTerm,
  youtube,
  usage,
  pageCreate,
  publishedVideo,
  plutoVideos,
  checkedFormFields,
  s3Upload,
  editState,
  uploads,
  routerReducer
});
