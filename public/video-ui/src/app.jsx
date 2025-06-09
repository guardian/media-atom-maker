import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import qs from 'querystringify';
import Raven from 'raven-js';
import { browserHistory } from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';
import { configureStore } from './util/configureStore';
import { setStore } from './util/storeAccessor';
import { extractConfigFromPage } from './util/config';
import { routes } from './routes';

import '../styles/main.scss';

const store = configureStore();
syncHistoryWithStore(browserHistory, store);
const config = extractConfigFromPage();

// publish uncaught errors to sentry.io
if (config.stage === 'PROD') Raven.config(config.ravenUrl).install();

setStore(store);

store.dispatch({
  type: 'CONFIG_RECEIVED',
  config: Object.assign({}, extractConfigFromPage(), {
    embeddedMode: qs.parse(location.search).embeddedMode
  }),
  receivedAt: Date.now()
});

store.dispatch({
  type: 'PATH_UPDATE',
  path: location.pathname,
  receivedAt: Date.now()
});

render(
  <Provider store={store}>
    {routes}
  </Provider>,
  document.getElementById('react-mount')
);
