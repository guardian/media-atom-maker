import React from 'react';

export default class CopyTrailButton extends React.Component {
  render() {
    return (
      <button
        type="button"
        disabled={!this.props.derivedFrom}
        className="btn form__label__button"
        onClick={() => this.props.onUpdateField(this.props.derivedFrom)}
      >
        <i className="icon">edit</i>
        <span data-tip="Copy trail text from description" />
      </button>
    );
  }
}
