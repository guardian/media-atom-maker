import React from 'react';

export default class TextArea extends React.Component {

  displayPlaceholder = () => {
    return this.props.placeholder === this.props.fieldValue;
  }

  renderField = () => {
    if(!this.props.editable) {
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className={"details-list__field " + (this.displayPlaceholder() ? "details-list__empty" : "")}> {this.props.fieldValue}</p>
        </div>
      );
    }

    const hasError = this.props.hasError(this.props);

    const hasWarning = this.props.hasWarning(this.props);

    function getTextAreaClassName() {

      if (hasError) {
        return "form__field form__field--error";
      }

      if (hasWarning) {
        return "form__field form__field--warning";
      }

      return "form__field";
    }

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <textarea rows="4"
          { ...this.props.input}
          maxLength={this.props.maxLength || ''}
          className={getTextAreaClassName()}
          type={this.props.inputType || "text"}
          value={this.props.fieldValue}
          onChange={(e) => {this.props.onUpdateField(e.target.value);}} />
        {hasError ? <p className="form__message form__message--error">{this.props.notification.message}</p> : ""}
        {hasWarning ? <p className="form__message form__message--warning">{this.props.notification.message}</p> : ""}
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
