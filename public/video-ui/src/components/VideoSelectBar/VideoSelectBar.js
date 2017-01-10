import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';

export default class VideoSelectBar extends React.Component {

  render() {
    if (!this.props.embeddedMode) {
       return false;
    }

    return (
      <div className="bar info-bar">
        <span className="bar__message"></span>
        <button type="button" className="bar__button" onClick={this.props.onSelectVideo}>Select this Video</button>
      </div>
    )
  }
}
