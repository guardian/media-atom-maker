import React from 'react';
import Picker from 'react-datepicker';
import moment from 'moment';
import 'react-datepicker/dist/react-datepicker.css';

export default React.createClass({

  handleChange(date) {
    this.setState({
      startDate: date
    });

    this.props.onUpdateField(date.valueOf());
  },

  getInitialState() {
    let startDate;

    if (parseInt(this.props.fieldValue)) {
      startDate = moment(this.props.fieldValue);
    } else {
      startDate = null;
    }

    return {
      startDate: startDate
    }
  },

  getReadableFieldValue(value) {
    if (parseInt(value)) {
      return moment(value).format('DD/MM/YYYY');
    }
    return value;
  },

  renderField() {
    if(!this.props.editable) {
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className="details-list__field">{this.getReadableFieldValue(this.props.fieldValue)}</p>
        </div>
      )
    }

    return (
      <div>
        <label className="form__label">{this.props.fieldName}</label>
        <Picker
          selected={this.state.startDate}
          onChange={this.handleChange}
          minDate={moment()}
          />
      </div>
    )
  },


  render() {
    return (
      <div>
        {this.renderField()}
      </div>
    )
  }
})
