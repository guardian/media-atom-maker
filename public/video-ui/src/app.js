import React from 'react';
import { render } from 'react-dom';
import {Router, browserHistory} from 'react-router';
import routes from './routes';

const element = document.getElementById('react-mount');

render(
    <Router
        routes={routes}
        history={browserHistory} />
    , element);
