import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';

export default class SaveButton extends React.Component {

  renderButtons = () => {
    if (this.props.isHidden) {
      return false;
    }

    return (
        <div className="btn__group">
          {this.saveButton()}
          {this.resetButton()}
        </div>
    );
  };

  isDisabled = () => {
    const errors = Object.keys(this.props.checkedFormFields).reduce((errors, fieldName) => {
      return errors.concat(this.props.checkedFormFields[fieldName]);
    }, []);
    return errors.length !== 0;
  };


  saveButton = () => {
    if(!this.props.onSaveClick) {
      return false;
    }

    return (
      <button
        type="button"
        disabled={this.isDisabled()}
        className={(this.props.saveState.saving == saveStateVals.inprogress ? "btn--loading " : "") + "btn"}
        onClick={this.props.onSaveClick}
      >
        <i className="i-tick-green"/>Save
      </button>
    );

  };

  resetButton = () => {
    if(!this.props.onResetClick) {
      return false;
    }

    return (
      <button type="button" className="btn" onClick={this.props.onResetClick}>
        <i className="i-cross-red"/>Reset
      </button>
    );
  };

  render () {
    return (
      <div>
        {this.renderButtons()}
      </div>
    );
  }
}
