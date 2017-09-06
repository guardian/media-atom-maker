import React from 'react';
import Picker from 'react-datepicker';
import moment from 'moment';
import 'react-datepicker/dist/react-datepicker.css';
import Icon from '../Icon';

const DATE_FORMAT = 'YYYY/MM/DD';
const DATETIME_FORMAT = `${DATE_FORMAT} HH:mm`;

const MINUTES = [0, 15, 30, 45].map(minute =>
  moment().minute(minute).format('mm')
);
const HOURS = [...new Array(24).keys()].map(hour =>
  moment().hour(hour).format('HH')
);
const EMPTY = [' '];

function Selector({ values, value, disabled, onChange }) {
  const handler = e => {
    onChange(e.target.value);
  };

  const options = values.map(value => {
    return <option key={value} value={value}>{value}</option>;
  });

  const params = {
    className: 'form__field form__field--select',
    disabled: disabled,
    value: value,
    onChange: handler
  };

  return <select {...params}>{options}</select>;
}

function HourSelector({ date, onChange }) {
  const params = {
    values: date ? HOURS : EMPTY,
    value: date ? date.format('HH') : ' ',
    disabled: !date,
    onChange: newHour => onChange(date.hours(newHour))
  };

  return <Selector {...params} />;
}

function MinuteSelector({ date, onChange }) {
  const params = {
    values: date ? MINUTES : EMPTY,
    value: date ? date.format('mm') : ' ',
    disabled: !date,
    onChange: newMinute => onChange(date.minutes(newMinute))
  };

  return <Selector {...params} />;
}

function DateSelector({ date, onChange }) {
  const datePickerParams = {
    className: 'form__field',
    selected: date,
    minDate: moment(),
    dateFormat: DATE_FORMAT,
    onChange: newDate => {
      const base = date ? date : moment().hours(0).minutes(0);
      onChange(newDate.hours(base.hours()).minutes(base.minutes()));
    }
  };

  return <Picker {...datePickerParams} />;
}

function Editor({ date, onChange, fieldName }) {
  function reset() {
    onChange(null);
  }

  return (
    <div>
      <label className="form__label">{fieldName}</label>
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
        <Icon
          icon="cancel"
          className="icon__edit expiry-date-picker__reset"
          onClick={reset}
        />
      </div>
    </div>
  );
}

function Display({ date, placeholder, fieldName }) {
  const displayString = date ? date.format(DATETIME_FORMAT) : placeholder;
  const fieldClassName = () =>
    'details-list__field' + (!date ? ' details-list__empty' : '');

  return (
    <div>
      <p className="details-list__title">{fieldName}</p>
      <p className={fieldClassName()}>
        {displayString}
      </p>
    </div>
  );
}

export default function DatePicker({
  editable,
  onUpdateField,
  fieldValue,
  placeholder,
  fieldName
}) {
  const date = fieldValue && fieldValue !== placeholder
    ? moment(fieldValue)
    : null;

  if (editable) {
    return (
      <Editor
        fieldName={fieldName}
        date={date}
        onChange={newDate => {
          if (newDate) {
            onUpdateField(newDate.valueOf());
          } else {
            onUpdateField(null);
          }
        }}
      />
    );
  } else {
    return <Display fieldName={fieldName} date={date} placeholder={placeholder} />;
  }
}
