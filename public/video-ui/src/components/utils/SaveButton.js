import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';

export default class SaveButton extends React.Component {

  renderButtons = () => {
    if (this.props.isHidden) {
      return false;
    }

    return (
        <div className="save">
          {this.saveButton()}
          {this.resetButton()}
        </div>
    );
  };

  saveButton = () => {
    if(this.props.onSaveClick) {
      return (
        <button type="button" className={(this.props.saveState == saveStateVals.inprogress ? "btn--loading " : "") + "btn"} onClick={this.props.onSaveClick}>
          <i className="i-tick-green"/>Save
        </button>
      )
    } else {
      return false;
    }
  };

  resetButton = () => {
    if(this.props.onResetClick) {
      return (
        <button type="button" className="btn" onClick={this.props.onResetClick}>
          <i className="i-cross-red"/>Reset
        </button>
      )
    } else {
      return false;
    }
  };

  render () {
    return (
      <div>
        {this.renderButtons()}
      </div>
    );
  }
}
