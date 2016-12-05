import React from 'react';

export default class SelectBox extends React.Component {

  renderField = () => {
    if(!this.props.editable) {
      const matchingValues = this.props.selectValues.filter((fieldValue) => fieldValue.id.toString() === this.props.fieldValue.toString())
      const displayValue = matchingValues.length ? matchingValues[0].title : this.props.fieldValue
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className="details-list__field">{displayValue}</p>
        </div>
      )
    }

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <select
          {...this.props.input}
          className={"form__field form__field--select " + (hasError ? "form__field--error" : "") }
          value={this.props.fieldValue}
          onChange={this.props.onUpdateField}>

          <option value=''>{this.props.defaultOption || "Please select..."}</option>

          {this.props.selectValues.map(function(value) {
            return (
              <option value={value.id} key={value.id}>{value.title}</option>
            );
          })}
        </select>
        {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
      </div>
    )
  };


  render() {
    return (
      <div>
        {this.renderField()}
      </div>
    )
  }
}
