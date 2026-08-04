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
const { stage, ravenUrl, sentryLocalEnabled } = getAppConfig();
const sentryEnvironment = stage.toLowerCase();

// publish uncaught errors to sentry.io
if (
  sentryEnvironment === 'code' ||
  sentryEnvironment === 'prod' ||
  (sentryEnvironment === 'dev' && sentryLocalEnabled)
) {
  Sentry.init({ dsn: ravenUrl, environment: sentryEnvironment });
}

setStore(store);

store.dispatch(updatePath(location.pathname));

render(
  <Provider store={store}>{routes}</Provider>,
  document.getElementById('react-mount')
);
