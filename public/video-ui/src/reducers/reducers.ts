import { routerReducer } from 'react-router-redux';
import config from '../slices/config';
import error from '../slices/error';
import video from '../slices/video';
import publishedVideo from './publishedVideoReducer';
import videos from '../slices/videos';
import saveState from '../slices/saveState';
import search from '../slices/search';
import youtube from '../slices/youtube';
import usage from './usageReducer';
import pageCreate from './composerPageReducer';
import s3Upload from './s3UploadReducer';
import videoEditOpen from '../slices/editState';
import checkedFormFields from '../slices/checkedFormFields';
import formFieldsWarning from './formFieldsWarningReducer';
import uploads from './uploadsReducer';
import path from '../slices/path';
import pluto from '../slices/pluto';
import workflow from './workflowReducer';
import targeting from './targetingReducer';

export default {
  config,
  error,
  video,
  videos,
  saveState,
  search,
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
