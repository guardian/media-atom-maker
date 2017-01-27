import React from 'react';
import {Router, Route, IndexRoute, browserHistory, IndexRedirect} from 'react-router';

import Videos from './components/Videos/Videos';
import VideoDisplay from './components/Video/Display';
import VideoCreate from './components/Video/Create';
import ReactApp from './components/ReactApp';


export const routes = (
  <Router history={browserHistory}>
    <Route path="/" component={ReactApp}>
      <IndexRedirect to="/videos" />
      <Route path="/videos" component={Videos} />
      <Route path="/videos/create" component={VideoCreate} />
      <Route path="/videos/:id" component={VideoDisplay} />
    </Route>
  </Router>
);
