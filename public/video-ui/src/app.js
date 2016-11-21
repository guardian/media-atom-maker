import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import qs from 'querystringify';

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

function isEmbeddedMode() {
  return qs.parse(location.search).embeddedMode === "true"
}

const store = configureStore();
const config = extractConfigFromPage();

setStore(store);

store.dispatch({
  type:       'CONFIG_RECEIVED',
  config:     Object.assign({}, extractConfigFromPage(), {
    embeddedMode: isEmbeddedMode()
  }),
  receivedAt: Date.now()
});


render(
    <Provider store={store}>
      {routes}
    </Provider>
    , document.getElementById('react-mount'));
