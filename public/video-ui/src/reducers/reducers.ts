import { routerReducer } from 'react-router-redux';
import config from '../slices/config';
import error from './errorReducer';
import errorKey from './errorKeyReducer';
import video from './videoReducer';
import publishedVideo from './publishedVideoReducer';
import videos from './videosReducer';
import saveState from './saveStateReducer';
import searchTerm from './searchTermReducer';
import youtube from '../slices/youtube';
import usage from './usageReducer';
import pageCreate from './composerPageReducer';
import s3Upload from './s3UploadReducer';
import videoEditOpen from './editStateReducer';
import plutoVideos from './plutoVideosReducer';
import checkedFormFields from './checkedFormFieldsReducer';
import formFieldsWarning from './formFieldsWarningReducer';
import uploads from './uploadsReducer';
import path from './pathReducer';
import pluto from '../slices/pluto';
import workflow from './workflowReducer';
import targeting from './targetingReducer';
import shouldUseCreatedDateForSort from './shouldUseCreatedDateForSortReducer';

export default {
  config,
  error,
  errorKey,
  video,
  videos,
  saveState,
  searchTerm,
  shouldUseCreatedDateForSort,
  youtube,
  usage,
  pageCreate,
  publishedVideo,
  plutoVideos,
  checkedFormFields,
  formFieldsWarning,
  s3Upload,
  videoEditOpen,
  uploads,
  path,
  routing: routerReducer,
  pluto,
  workflow,
  targeting
};
