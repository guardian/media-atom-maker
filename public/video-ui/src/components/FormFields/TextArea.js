import React from 'react';
import CopyTrailButton from '../FormComponents/CopyTrailButton';

export default class TextArea extends React.Component {
  renderCopyButton = () => {
    if (this.props.derivedFrom === undefined) {
      return null;
    }

    return (
      <CopyTrailButton
        onUpdateField={this.props.onUpdateField}
        derivedFrom={this.props.derivedFrom}
      />
    );
  };

  renderField = () => {
    if (!this.props.editable) {
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p
            className={
              'details-list__field ' +
                (this.props.displayPlaceholder(
                  this.props.placeholder,
                  this.props.fieldValue
                )
                  ? 'details-list__empty'
                  : '')
            }
          >
            {this.props.fieldValue}
          </p>
        </div>
      );
    }

    const hasError = this.props.hasError(this.props);

    const hasWarning = this.props.hasWarning(this.props);

    function getTextAreaClassName() {
      if (hasError) {
        return 'form__field form__field--error';
      }

      if (hasWarning) {
        return 'form__field form__field--warning';
      }

      return 'form__field';
    }

    return (
      <div className="form__row">
        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
          {this.renderCopyButton()}
        </div>
        <textarea
          rows="4"
          maxLength={this.props.maxLength || ''}
          className={getTextAreaClassName()}
          type={this.props.inputType || 'text'}
          value={this.props.fieldValue}
          onChange={e => {
            this.props.onUpdateField(e.target.value);
          }}
        />
        {hasError
          ? <p className="form__message form__message--error">
              {this.props.notification.message}
            </p>
          : ''}
        {hasWarning
          ? <p className="form__message form__message--warning">
              {this.props.notification.message}
            </p>
          : ''}
      </div>
    );
  };

  render() {
    return (
      <div>
        {this.renderField()}
      </div>
    );
  }
}
