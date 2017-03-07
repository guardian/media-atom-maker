import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';
import {validate} from '../../constants/videoEditValidation';

export default class SaveButton extends React.Component {

  createDisabled = () => {
    if (!this.props.video) {
      return false;
    }
    return Object.keys(validate(Object.assign(this.props.video, {
      youtubeCategory: this.props.video.youtubeCategoryId,
      youtubeChannel: this.props.video.channelId
    }))).length !== 0;
  }

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

  saveButton = () => {
    if(!this.props.onSaveClick) {
      return false;
    }


    return (
      <button
        type="button"
        disabled={this.createDisabled()}
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
