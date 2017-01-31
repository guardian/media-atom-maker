import React from 'react';
import moment from 'moment';
import DatePickerField from '../../FormFields/DatePicker';

export default class VideoExpiryEdit extends React.Component {

  onUpdateExpiry = (newDate) => {
    const newData = Object.assign({}, this.props.video, {
      expiryDate: newDate
    });

    this.props.saveAndUpdateVideo(newData);
  };

  render () {
    if (!this.props.video) {
      return false;
    }

    return (
        <DatePickerField
          fieldName="Expiry Date"
          fieldValue={this.props.video.expiryDate || 'No expiry date set'}
          onUpdateField={this.onUpdateExpiry}
          {...this.props} />
    );
  }
}
