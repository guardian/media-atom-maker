import { configureStore } from '@reduxjs/toolkit';
import { browserHistory } from 'react-router';
import { routerMiddleware, routerReducer } from 'react-router-redux';
import checkedFormFields from '../slices/checkedFormFields';
import config from '../slices/config';
import videoEditOpen from '../slices/editState';
import error from '../slices/error';
import formFieldsWarning from '../slices/formFieldsWarning';
import { iconikReducer } from '../slices/iconik';
import path from '../slices/path';
import pluto from '../slices/pluto';
import s3Upload from '../slices/s3Upload';
import search from '../slices/search';
import targeting from '../slices/targeting';
import uploads from '../slices/uploads';
import usage from '../slices/usage';
import video from '../slices/video';
import videos from '../slices/videos';
import workflow from '../slices/workflow';
import youtube from '../slices/youtube';
import { storeMiddleware } from './storeMiddleware';

export function setupStore() {
  return configureStore({
    reducer: {
      config,
      error,
      video,
      videos,
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
      targeting,
      iconik: iconikReducer
    },
    middleware: getDefaultMiddleware => {
      return getDefaultMiddleware().concat(
        routerMiddleware(browserHistory),
        storeMiddleware
      );
    }
  });
}

export type RootState = ReturnType<ReturnType<typeof setupStore>['getState']>;
export type AppDispatch = ReturnType<typeof setupStore>['dispatch'];
