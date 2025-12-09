import React from 'react';
import {
  browserHistory,
  IndexRedirect,
  Redirect,
  Route,
  Router
} from 'react-router';

import { RouterState } from 'react-router/lib/Router';
import { ReactApp } from './components/ReactApp';
import Help from './pages/Help';
import Search from './pages/Search';
import Training from './pages/Training';
import { VideoUpload } from './pages/Upload';
import Video from './pages/Video';
import { extractConfigFromPage, getUserTelemetryClient } from './util/config';
import Create from "./components/Create";
import {bindActionCreators} from "redux";
import {createVideo} from "./actions/VideoActions/createVideo";
import {useDispatch} from "react-redux";
import {AppDispatch} from "./util/setupStore";

const telemetryClientUrl = getUserTelemetryClient(
  extractConfigFromPage().stage
);


function loadTrackingPixel(clientUrl: string, path: string) {
  const image = new Image();
  image.src = `${clientUrl}/guardian-tool-accessed?app=video&path=${path}`;
}

function sendTelemetry(state: RouterState) {
  loadTrackingPixel(telemetryClientUrl, state.location.pathname);
  return state;
}

const CreateWrapper: React.FC<any> = (props) => {
  const dispatch = useDispatch<AppDispatch>();
  const createVideoBound = bindActionCreators(createVideo, dispatch);
  return <Create {...props} inModal={false} createVideo={createVideoBound} />;
};

export const routes = (
  <Router history={browserHistory}>
    <Route path="/" component={ReactApp}>
      <IndexRedirect to="/videos" />
      <Route path="/videos" component={Search} onEnter={sendTelemetry} />
      <Redirect from="/videos/create" to="/create" />
      <Route path="/videos/:id" component={Video} onEnter={sendTelemetry} />
      <Route
        path="/videos/:id/upload"
        component={VideoUpload}
        onEnter={sendTelemetry}
      />
      <Route
        path="/create"
        component={CreateWrapper}
        onEnter={sendTelemetry}
      />
      <Route path="/help" component={Help} onEnter={sendTelemetry} />
      <Route path="/training" component={Training} onEnter={sendTelemetry} />
      <Route path="/backfill" component={Search} onEnter={sendTelemetry} />
    </Route>
  </Router>
);
