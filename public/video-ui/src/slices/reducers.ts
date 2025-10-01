import { routerReducer } from 'react-router-redux';
import config from './config';
import error from './error';
import video from './video';
import videos from './videos';
import saveState from './saveState';
import search from './search';
import youtube from './youtube';
import usage from './usage';
import s3Upload from './s3Upload';
import formFieldsWarning from './formFieldsWarning';
import videoEditOpen from './editState';
import checkedFormFields from './checkedFormFields';
import uploads from './uploads';
import path from './path';
import pluto from './pluto';
import targeting from './targeting';
import workflow from './workflow';

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
