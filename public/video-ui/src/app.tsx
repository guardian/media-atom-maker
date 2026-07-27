import * as Sentry from '@sentry/browser';
import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import { browserHistory } from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';
import { routes } from './routes';
import { updatePath } from './slices/path';
import { getAppConfig } from './util/config';
import { setupStore } from './util/setupStore';
import { setStore } from './util/storeAccessor';

import '../styles/main.scss';

const store = setupStore();
syncHistoryWithStore(browserHistory, store);
const { stage, ravenUrl } = getAppConfig();

// Publish uncaught errors to Sentry in non-local environments.
const sentryEnabled = stage !== 'DEV' && !!ravenUrl;
if (sentryEnabled) {
  Sentry.init({
    dsn: ravenUrl,
    environment: stage,
    attachStacktrace: true
  });
}

setStore(store);

store.dispatch(updatePath(location.pathname));

render(
  <Provider store={store}>{routes}</Provider>,
  document.getElementById('react-mount')
);
