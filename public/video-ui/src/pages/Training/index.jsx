import React from 'react';
import { getStore } from '../../util/storeAccessor';

export default class Training extends React.Component {
  render() {
    const trainingModeState = getStore().getState().config.isTrainingMode
      ? 'on'
      : 'off';

    return (
      <div className="container">
        <h1>Training mode is currently {trainingModeState}</h1>
        <ul>
          <li>
            <a href="/training/on">Turn on</a>
          </li>
          <li>
            <a href="/training/off">Turn off</a>
          </li>
        </ul>
      </div>
    );
  }
}
