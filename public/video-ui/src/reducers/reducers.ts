import { routerReducer } from 'react-router-redux';
import config from '../slices/config';
import error from '../slices/error';
import video from '../slices/video';
import publishedVideo from './publishedVideoReducer';
import videos from '../slices/videos';
import saveState from '../slices/saveState';
import searchTerm from './searchTermReducer';
import youtube from '../slices/youtube';
import usage from './usageReducer';
import pageCreate from './composerPageReducer';
import s3Upload from './s3UploadReducer';
import videoEditOpen from './editStateReducer';
import checkedFormFields from './checkedFormFieldsReducer';
import formFieldsWarning from './formFieldsWarningReducer';
import uploads from './uploadsReducer';
import path from '../slices/path';
import pluto from '../slices/pluto';
import workflow from './workflowReducer';
import targeting from './targetingReducer';
import shouldUseCreatedDateForSort from './shouldUseCreatedDateForSortReducer';
import mediaPlatformFilter from "./mediaPlatformReducer";

export default {
  config,
  error,
  video,
  videos,
  saveState,
  searchTerm,
  shouldUseCreatedDateForSort,
  mediaPlatformFilter,
  youtube,
  usage,
  pageCreate,
  publishedVideo,
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
