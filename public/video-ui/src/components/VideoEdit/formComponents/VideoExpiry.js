import React from 'react';
import Picker from 'react-datepicker';
import moment from 'moment';
import 'react-datepicker/dist/react-datepicker.css';

export default class VideoExpiryEdit extends React.Component {

  constructor(props) {
    super(props);

    this.fieldName = "Expiry Date";
    this.validHours = [...new Array(24).keys()];
    this.validMinutes = [0, 15, 30, 45];

    this.readExpiryDate(props);
  }

  componentWillReceiveProps(props) {
    this.readExpiryDate(props);
  }

  readExpiryDate(props) {
    const date = props.video.expiryDate;
    const midnightToday = moment().hours(0).minutes(0).seconds(0);

    this.hasExpiryDate = date !== undefined;
    this.date = date ? moment(date) : midnightToday;
  }

  setExpiryDate(newDate) {
    const newVideo = Object.assign({}, this.props.video, { expiryDate: newDate.valueOf() });
    this.props.updateVideo(newVideo);
  }

  renderDisplay() {
    const displayString = this.hasExpiryDate ? this.date.format("DD/MM/YYYY HH:mm") : 'No expiry date set';

    return <div>
      <p className="details-list__title">{this.fieldName}</p>
      <p className="details-list__field">{displayString}</p>
    </div>
  }

  selector(values, selected, formatter, onChange) {
    const handler = (e) => {
      onChange(e.target.value);
    };

    const options = values.map((value) => {
      return <option key={value} value={value}>{formatter(value)}</option>;
    });

    return <select className="form__field form__field--select" value={selected} onChange={handler}>{options}</select>;
  }

  renderPicker() {
    const onDateChange = (newDate) => {
      this.setExpiryDate(newDate.hours(this.date.hours()).minutes(this.date.minutes()));
    };

    const hourPicker = this.selector(this.validHours, this.date.hour(),
      (hour) => moment().hour(hour).format("HH"),
      (newHour) => {
        this.setExpiryDate(this.date.hours(newHour))
      }
    );

    const minutePicker = this.selector(this.validMinutes, this.date.minute(),
      (minute) => moment().minute(minute).format("mm"),
      (newMinute) => this.setExpiryDate(this.date.minutes(newMinute))
    );

    return (
      <div>
        <label className="form__label">{this.fieldName}</label>
        <Picker
          selected={this.date}
          onChange={onDateChange}
          minDate={moment()}
          className="form__field"
        />
        {hourPicker}
        {minutePicker}
      </div>
    );
  }

  render () {
    if (!this.props.video) {
      return false;
    }

    if(this.props.editable) {
      return this.renderPicker();
    } else {
      return this.renderDisplay();
    }
  }
}
