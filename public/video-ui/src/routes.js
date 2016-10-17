import React from 'react';
import {Router, Route, IndexRoute, browserHistory, IndexRedirect} from 'react-router';

import Videos from './components/Videos/Videos';
import VideoEdit from './components/VideoEdit/VideoEdit';
import ReactApp from './components/ReactApp';


export const routes = (
  <Router history={browserHistory}>
    <Route path="/video" component={ReactApp}>
      <IndexRedirect to="/video/videos" />
      <Route path="/video/videos" component={Videos} />
      <Route path="/video/videos/:id" component={VideoEdit} />
    </Route>
  </Router>
);
