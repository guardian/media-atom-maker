import { browserHistory } from 'react-router';
import { routerMiddleware, routerReducer } from 'react-router-redux';
import { storeMiddleware } from './storeMiddleware';
import { configureStore } from '@reduxjs/toolkit';
import config from '../slices/config';
import error from '../slices/error';
import video from '../slices/video';
import videos from '../slices/videos';
import search from '../slices/search';
import youtube from '../slices/youtube';
import usage from '../slices/usage';
import checkedFormFields from '../slices/checkedFormFields';
import formFieldsWarning from '../slices/formFieldsWarning';
import s3Upload from '../slices/s3Upload';
import videoEditOpen from '../slices/editState';
import uploads from '../slices/uploads';
import path from '../slices/path';
import pluto from '../slices/pluto';
import workflow from '../slices/workflow';
import targeting from '../slices/targeting';

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
      targeting
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
