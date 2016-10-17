import React from 'react';
import {Router, Route, IndexRoute, browserHistory, IndexRedirect} from 'react-router';

import Atoms from './components/Atoms/Atoms';
import AtomEdit from './components/AtomEdit/AtomEdit';
import ReactApp from './components/ReactApp';


export const routes = (
  <Router history={browserHistory}>
    <Route path="/video" component={ReactApp}>
      <IndexRedirect to="/video/atoms" />
      <Route path="/video/atoms" component={Atoms} />
      <Route path="/video/atoms/:id" component={AtomEdit} />
    </Route>
  </Router>
);
