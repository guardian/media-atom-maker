import React from 'react';
import DatePicker from '../../FormFields/DatePicker';

export default class VideoExpiryEdit extends React.Component {

  onUpdateExpiry = (newDate) => {
    const newData = Object.assign({}, this.props.video, {
      expiryDate: newDate
    });

    this.props.updateVideo(newData);
  };

  render () {
    if (!this.props.video) {
      return false;
    }

    return (
        <DatePicker
          fieldName="Expiry Date"
          fieldValue={this.props.video.expiryDate || 'No expiry date set'}
          onUpdateField={this.onUpdateExpiry}
          {...this.props} />
    );
  }
}
