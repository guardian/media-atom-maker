import { routerReducer } from 'react-router-redux';
import config from '../slices/config';
import error from '../slices/error';
import video from '../slices/video';
import videos from '../slices/videos';
import saveState from '../slices/saveState';
import search from '../slices/search';
import youtube from '../slices/youtube';
import usage from '../slices/usage';
import s3Upload from '../slices/s3Upload';
import formFieldsWarning from '../slices/formFieldsWarning';
import videoEditOpen from '../slices/editState';
import checkedFormFields from '../slices/checkedFormFields';
import uploads from '../slices/uploads';
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
