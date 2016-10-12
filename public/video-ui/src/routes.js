import React from 'react';
import {Route, IndexRoute} from 'react-router';

import Atoms from './components/Atoms/Atoms';
import AtomEdit from './components/AtomEdit/AtomEdit';

import ReactApp from './components/ReactApp';


export default [
  <Route path='/beta' component={ReactApp}>
    <Route path='/beta/atoms'       component={Atoms} />
    <Route path='/beta/atoms/:id'   component={AtomEdit} />
    <IndexRoute                     component={Atoms} />
  </Route>
];
