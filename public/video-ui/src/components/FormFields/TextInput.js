import React from 'react';

export default class TextInput extends React.Component {

  renderField = () => {
    if(!this.props.editable) {
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className="details-list__field">{this.props.fieldValue}</p>
        </div>
      );
    }

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <input
          { ...this.props.input}
          maxLength={this.props.maxLength || ''}
          className={"form__field " + (hasError ? "form__field--error" : "")}
          type={this.props.inputType || "text"}
          value={this.props.fieldValue}
          onChange={this.props.onUpdateField} />
        {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
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
