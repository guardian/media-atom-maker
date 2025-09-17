import { routerReducer } from 'react-router-redux';
import config from '../slices/config';
import error from '../slices/error';
import errorKey from './errorKeyReducer';
import video from './videoReducer';
import publishedVideo from './publishedVideoReducer';
import videos from '../slices/videos';
import saveState from './saveStateReducer';
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
import workflow from '../slices/workflow';
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
