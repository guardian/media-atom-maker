import React from 'react';

export default class SaveButton extends React.Component {
  isDisabled = () => {
    const errors = Object.keys(
      this.props.checkedFormFields
    ).reduce((errors, fieldName) => {
      const error = this.props.checkedFormFields[fieldName];

      if (error) {
        return errors.concat(error);
      }

      return errors;
    }, []);
    return errors.length !== 0;
  };

  renderSaveButton = () => {
    if (this.props.isHidden) {
      return false;
    }

    if (!this.props.onSaveClick) {
      return false;
    }

    return (
      <button
        type="button"
        disabled={this.isDisabled()}
        className={
          (this.props.saveState.saving
            ? 'btn--loading '
            : '') + 'btn'
        }
        onClick={this.props.onSaveClick}
      >
        <i className="i-tick-green" />Save
      </button>
    );
  };

  render() {
    return (
      <div>
        {this.renderSaveButton()}
      </div>
    );
  }
}
