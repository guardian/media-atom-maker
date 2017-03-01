import React from 'react';
import Picker from 'react-datepicker';
import moment from 'moment';
import _ from 'lodash';
import 'react-datepicker/dist/react-datepicker.css';
import Icon from '../../Icon';

function Selector({values, value, disabled, onChange}) {
  const handler = (e) => {
    onChange(e.target.value);
  };

  const options = values.map((value) => {
    return <option key={value} value={value}>{value}</option>;
  });

  const params = {
    className: "form__field form__field--select",
    disabled: disabled,
    value: value,
    onChange: handler
  };

  return <select {...params}>{options}</select>;
}

function HourSelector({date, onChange}) {
  let values = [" "];
  if (date) {
    values = [...new Array(24).keys()].map((hour) => moment().hour(hour).format("HH"));
  }

  const params = {
    values: values,
    value: date ? date.format("HH") : " ",
    disabled: !date,
    onChange: (newHour) => onChange(date.hours(newHour))
  };

  return <Selector {...params} />;
}

function MinuteSelector({date, onChange}) {
  let values = [" "];
  if (date) {
    values = [0, 15, 30, 45].map((minute) => moment().minute(minute).format("mm"));
  }

  const params = {
    values: values,
    value: date ? date.format("mm") : " ",
    disabled: !date,
    onChange: (newMinute) => onChange(date.minutes(newMinute))
  };

  return <Selector {...params} />;
}

function DateSelector({date, onChange}) {
  const datePickerParams = {
    className: "form__field",
    selected: date,
    minDate: moment(),
    dateFormat: "DD/MM/YYYY",
    onChange: (newDate) => {
      const base = date ? date : moment().hours(0).minutes(0);
      onChange(newDate.hours(base.hours()).minutes(base.minutes()));
    }
  };

  return <Picker {...datePickerParams} />;
}

function Editor({date, onChange}) {
  function reset() {
    onChange(null);
  }

  return (
      <div>
        <label className="form__label">Expiry Date</label>
        <div className="expiry-date-picker">
          <div className="expiry-date-picker__date">
            <DateSelector date={date} onChange={onChange} />
          </div>
          <div className="expiry-date-picker__number">
            <HourSelector date={date} onChange={onChange} />
          </div>
          <div className="expiry-date-picker__number">
            <MinuteSelector date={date} onChange={onChange} />
          </div>
          <Icon icon="cancel" className="icon__edit expiry-date-picker__reset" onClick={reset} />
        </div>
      </div>
    );
}

function Display({date}) {
  const displayString = date ? date.format("DD/MM/YYYY HH:mm") : 'No expiry date set';

  return <div>
    <p className="details-list__title">Expiry Date</p>
    <p className="details-list__field">{displayString}</p>
  </div>;
}

export default function VideoExpiryEdit({editable, video, updateVideo}) {
  function setExpiryDate(newDate) {
    if(newDate) {
      const newVideo = Object.assign({}, video, { expiryDate: newDate.valueOf() });
      updateVideo(newVideo);
    } else {
      const newVideo = _.omit(video, 'expiryDate');
      updateVideo(newVideo);
    }
  }

  if (!video) {
    return false;
  }

  const date = video.expiryDate ? moment(video.expiryDate) : null;

  if(editable) {
    return <Editor date={date} onChange={setExpiryDate} />;
  } else {
    return <Display date={date} />;
  }
}
