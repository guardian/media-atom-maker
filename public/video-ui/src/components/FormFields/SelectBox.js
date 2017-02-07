import React from 'react';

export default class SelectBox extends React.Component {

  hasError = () => {
    return this.props.meta.touched && this.props.meta.error;
  };

  getClassName = () => {

    return "form__field form__field--select " + (this.hasError() ? "form__field--error" : "") + (this.props.hasNotifications ? "form__field--notification" : "");
  }

  renderNotification = () => {
    if (this.props.hasNotifications) {
      return (
        <span className="details-list__notification-text">{this.props.notificationMessage}</span>
        );
    }
  }
  renderDefaultOption = () => {
    if (this.props.displayDefault || this.props.fieldValue === "") {
      return (
        <option value="">{this.props.defaultOption || "Please select..."}</option>
      );
    }
  }

  renderField = () => {
    if(!this.props.editable) {
      const matchingValues = this.props.selectValues.filter((fieldValue) => this.props.fieldValue && (fieldValue.id.toString() === this.props.fieldValue.toString()));
      const displayValue = matchingValues.length ? matchingValues[0].title : this.props.fieldValue;
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className={"details-list__field" + (this.props.hasNotifications ? " details-list__notification" : "")}>{displayValue}</p>
          {this.renderNotification()}
        </div>
      );
    }

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <select
          {...this.props.input}
          className={this.getClassName()}
          value={this.props.fieldValue}
          onChange={this.props.onUpdateField}>


          {this.renderDefaultOption()}
          {this.props.selectValues.map(function(value) {
            return (
              <option value={value.id} key={value.id}>{value.title}</option>
            );
          })}
        </select>
        {this.renderNotification()}
        {this.hasError() ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
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
