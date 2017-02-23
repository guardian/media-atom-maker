import React from 'react';

export default class TextArea extends React.Component {

  renderField = () => {
    if(!this.props.editable) {
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className={"details-list__field " + (this.props.noValue ? "details-list__empty" : "")}> {this.props.fieldValue}</p>
        </div>
      );
    }

    const hasError = this.props.meta.touched && this.props.meta.error;
    const hasWarning = this.props.meta.touched && this.props.meta.warning;

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <textarea rows="4"
          { ...this.props.input}
          maxLength={this.props.maxLength || ''}
          className={"form__field " + (hasError ? "form__field--error" : "") + (hasWarning ? "form__field--warning" : "")}
          type={this.props.inputType || "text"}
          value={this.props.fieldValue}
          onChange={this.props.onUpdateField} />
        {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        {hasWarning ? <p className="form__message form__message--warning">{this.props.meta.warning}</p> : ""}
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
