import React from 'react';

export default class TextInput extends React.Component {

  renderField = () => {
    if(this.props.editable) {
      const hasError = this.props.meta.touched && this.props.meta.error;

      return (
        <div className="form__row">
          <label className="form__label">{this.props.fieldLabel}</label>
          <input
          { ...this.props.input}
          className={"form__field " + (hasError ? "form__field--error" : "")}
          type="text" value={this.props.fieldValue}
          onChange={this.props.onUpdateTitle} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
      )
    } else {
      return (
        <div>
          <p className="details-list__title">{this.props.fieldLabel}</p>
          <p className="details-list__field">{this.props.fieldValue}</p>
        </div>
      )
    }
  };


  render() {
    return (
      <div>
        {this.renderField()}
      </div>
    )
  }
}
