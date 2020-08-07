import React from 'react';
import DatePicker from 'react-datepicker';
import moment from 'moment';
import 'react-datepicker/dist/react-datepicker.css';
import Icon from '../Icon';

const DATE_FORMAT = 'dd MMM yyyy';
const DATETIME_FORMAT = `DD MMM yyyy HH:mm`;

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
    return (
      <option key={value} value={value}>
        {value}
      </option>
    );
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
  const minDate = moment().toDate();
  const datePickerParams = {
    className: 'form__field',
    selected: date ? date.toDate() : null,
    minDate,
    dateFormat: DATE_FORMAT,
    readOnly: false,
    onChange: newDate => {
      const base = date ? date : moment().hours(0).minutes(0);
      const onChangeDate = moment(newDate)
        .hours(base.hours())
        .minutes(base.minutes());
      onChange(onChangeDate);
    }
  };

  return <DatePicker {...datePickerParams} />;
}

function Editor({ date, onChange, fieldName, canCancel, dayOnly }) {
  function reset() {
    onChange(null);
  }

  return (
    <div>
      {fieldName && <label className="form__label">{fieldName}</label>}
      <div className="expiry-date-picker">
        <div className="expiry-date-picker__date">
          <DateSelector date={date} onChange={onChange} />
        </div>
        {!dayOnly && (
          <div className="expiry-date-picker__number">
            <HourSelector date={date} onChange={onChange} />
          </div>
        )}
        {!dayOnly && (
          <div className="expiry-date-picker__number">
            <MinuteSelector date={date} onChange={onChange} />
          </div>
        )}
        {date && canCancel && (
          <Icon
            icon="cancel"
            className="icon__edit icon__cancel"
            onClick={reset}
          />
        )}
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
      <p className={fieldClassName()}>{displayString}</p>
    </div>
  );
}

export default function CustomDatePicker({
  editable,
  onUpdateField,
  fieldValue,
  placeholder,
  fieldName,
  dayOnly,
  canCancel = true
}) {
  const date =
    fieldValue && fieldValue !== placeholder ? moment(fieldValue) : null;

  if (editable) {
    return (
      <Editor
        fieldName={fieldName}
        date={date}
        placeholder={placeholder}
        canCancel={canCancel}
        dayOnly={dayOnly}
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
    return (
      <Display fieldName={fieldName} date={date} placeholder={placeholder} />
    );
  }
}
