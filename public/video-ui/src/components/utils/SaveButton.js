import React from 'react';
import ProgressSpinner from './ProgressSpinner';
import {saveStateVals} from '../../constants/saveStateVals';

export default class SaveButton extends React.Component {

  constructor(props) {
    super(props);
  }

  renderSaveStateIndicator() {
    if (this.props.saveState == saveStateVals.inprogress) {
      return (
          <div className="save__button--indicator">
            <ProgressSpinner />
          </div>
      );
    }

    return false;
  }

  renderButtons() {
    if (this.props.isHidden) {
      return false;
    }

    return (
        <div className="save">
          <div className="btn" onClick={this.props.onSaveClick}>
            <i className="i-tick-green"/>Save
          </div>
          <div className="btn" onClick={this.props.onResetClick}>
            <i className="i-cross-red"/>Reset
          </div>
          {this.renderSaveStateIndicator()}
        </div>
    );
  }

  render () {
    return (
      <div>
        {this.renderButtons()}
      </div>
    );
  }
}
