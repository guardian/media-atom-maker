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

    const hasError = this.props.touched && this.props.errors.length > 0;

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <textarea rows="4"
          { ...this.props.input}
          maxLength={this.props.maxLength || ''}
          className={"form__field " + (hasError ? "form__field--error" : "")}
          type={this.props.inputType || "text"}
          value={this.props.fieldValue}
          onChange={(e) => {this.props.onUpdateField(e.target.value);}} />
        {hasError ? <p className="form__message form__message--error">{this.props.errors[0].message}</p> : ""}
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
