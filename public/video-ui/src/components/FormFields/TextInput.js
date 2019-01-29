import React from 'react';

const id = val => val;

export default class TextInput extends React.Component {
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
            {' '}{this.props.fieldValue}
          </p>
        </div>
      );
    }

    const hasError = this.props.hasError(this.props);

    const map = this.props.map || id;

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <input
          {...this.props.input}
          maxLength={this.props.maxLength || ''}
          className={'form__field ' + (hasError ? 'form__field--error' : '')}
          type="text"
          value={this.props.fieldValue}
          onChange={e => {
            this.props.onUpdateField(map(e.target.value));
          }}
        />
        {hasError
          ? <p className="form__message form__message--error">
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
