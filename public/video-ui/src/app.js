import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import qs from 'querystringify';
import Raven from 'raven-js';

import configureStore from './util/configureStore';
import {setStore} from './util/storeAccessor';
import {routes} from './routes';

import '../styles/main.scss';

function extractConfigFromPage() {

  const configEl = document.getElementById('config');

  if (!configEl) {
    return {};
  }

  return JSON.parse(configEl.innerHTML);
}

const store = configureStore();
const config = extractConfigFromPage();

// publish uncaught errors to sentry.io
if(config.stage === 'PROD')
  Raven.config(config.ravenUrl).install();

setStore(store);

store.dispatch({
  type:       'CONFIG_RECEIVED',
  config:     Object.assign({}, extractConfigFromPage(), {
    embeddedMode: qs.parse(location.search).embeddedMode,
    isPreviewMode: qs.parse(location.search).embeddedMode === 'preview',
    isLiveMode: qs.parse(location.search).embeddedMode === 'live'
  }),
  receivedAt: Date.now()
});

render(
    <Provider store={store}>
      {routes}
    </Provider>
    , document.getElementById('react-mount'));
