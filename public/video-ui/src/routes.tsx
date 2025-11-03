import React from 'react';
import { browserHistory, IndexRedirect, Redirect, Route, Router } from 'react-router';

import { ReactApp } from './components/ReactApp';
import Help from './pages/Help';
import Search from './pages/Search';
import Training from './pages/Training';
import Upload from './pages/Upload';
import Video from './pages/Video';
import { extractConfigFromPage, getUserTelemetryClient } from './util/config';
import { RouterState } from "react-router/lib/Router";

const telemetryClientUrl = getUserTelemetryClient(extractConfigFromPage().stage);

function loadTrackingPixel(clientUrl: string, path: string) {
  const image = new Image();
  image.src = `${clientUrl}/guardian-tool-accessed?app=video&path=${path}`;
}

function sendTelemetry(state: RouterState) {
  loadTrackingPixel(telemetryClientUrl, state.location.pathname);
  return state
}

export const routes = (
  <Router history={browserHistory}>
    <Route path="/" component={ReactApp}>
      <IndexRedirect to="/videos" />
      <Route path="/videos" component={Search} onEnter={sendTelemetry} />
      <Redirect from="/videos/create" to="/create" />
      <Route path="/videos/:id" component={Video} onEnter={sendTelemetry} />
      <Route path="/videos/:id/upload" component={Upload} onEnter={sendTelemetry} />
      <Route path="/create" component={Video} props={{mode: "create"}} onEnter={sendTelemetry} />
      <Route path="/help" component={Help} onEnter={sendTelemetry} />
      <Route path="/training" component={Training} onEnter={sendTelemetry} />
    </Route>
  </Router>
);
