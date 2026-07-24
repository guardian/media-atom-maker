import React from 'react';
// @ts-expect-error TS(7016): Could not find a declaration file for module 'reac... Remove this comment to see the full error message
import DatePicker from 'react-datepicker';
import moment from 'moment';
import 'react-datepicker/dist/react-datepicker.css';
import Icon from '../Icon';

const REACT_DATEPICKER_DATE_FORMAT = 'dd MMM yyyy';
const MOMENT_DATETIME_FORMAT = `DD MMM YYYY HH:mm`;

const MINUTES = [0, 15, 30, 45].map(minute =>
  moment().minute(minute).format('mm')
);
const HOURS = [...new Array(24).keys()].map(hour =>
  moment().hour(hour).format('HH')
);
const EMPTY = [' '];

function Selector({ values, value, disabled, onChange }: any) {
  const handler = (e: any) => {
    onChange(e.target.value);
  };

  const options = values.map((value: any) => {
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

function HourSelector({
  date,
  onChange
}: {
  date: number;
  onChange: (newDate: any) => void;
}) {
  const dateMoment = moment(date);
  const params = {
    values: date ? HOURS : EMPTY,
    value: date ? dateMoment.format('HH') : ' ',
    disabled: !date,
    onChange: (newHour: number) => onChange(dateMoment.hours(newHour))
  };

  return <Selector {...params} />;
}

function MinuteSelector({
  date,
  onChange
}: {
  date: number;
  onChange: (newDate: any) => void;
}) {
  const dateMoment = moment(date);
  const params = {
    values: date ? MINUTES : EMPTY,
    value: date ? dateMoment.format('mm') : ' ',
    disabled: !date,
    onChange: (newMinute: number) => onChange(dateMoment.minutes(newMinute))
  };

  return <Selector {...params} />;
}

function DateSelector({
  date,
  onChange
}: {
  date: number;
  onChange: (newDate: any) => void;
}) {
  const minDate = moment().toDate();
  const dateMoment = moment(date);
  const datePickerParams = {
    className: 'form__field',
    selected: date ? dateMoment.toDate() : null,
    minDate,
    dateFormat: REACT_DATEPICKER_DATE_FORMAT,
    readOnly: false,
    onChange: (newDate: moment.MomentInput) => {
      const base = date ? dateMoment : moment().hours(0).minutes(0);
      const onChangeDate = moment(newDate)
        .hours(base.hours())
        .minutes(base.minutes())
        .seconds(0)
        .milliseconds(0);
      onChange(onChangeDate);
    }
  };

  return <DatePicker {...datePickerParams} />;
}

function Editor({
  date,
  onChange,
  fieldName,
  canCancel,
  dayOnly
}: {
  date: number;
  onChange: (newDate: any) => void;
  fieldName: any;
  canCancel: boolean;
  dayOnly: any;
}) {
  function reset() {
    onChange(null);
  }

  return (
    <fieldset>
      {fieldName && <legend className="form__label">{fieldName}</legend>}
      <p className="form__message form__message--display">
        Expiring the video will make it private for viewing in Youtube, so it
        will not be available in the video page.
      </p>
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
    </fieldset>
  );
}

function Display({
  date,
  placeholder,
  fieldName
}: {
  date: number;
  placeholder: any;
  fieldName: any;
}) {
  const dateMoment = moment(date);
  const displayString = date
    ? dateMoment.format(MOMENT_DATETIME_FORMAT)
    : placeholder;
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
}: {
  editable: boolean;
  onUpdateField: (date: number) => void;
  fieldValue: number;
  placeholder: null;
  fieldName: string;
  dayOnly: boolean;
  canCancel: boolean;
}) {
  const date =
    fieldValue && fieldValue !== placeholder
      ? moment(fieldValue).valueOf()
      : null;
  if (editable) {
    return (
      <Editor
        fieldName={fieldName}
        date={date}
        // @ts-expect-error TS(2322): Type '{ fieldName: string; date: number; placehold... Remove this comment to see the full error message
        placeholder={placeholder}
        canCancel={canCancel}
        dayOnly={dayOnly}
        onChange={(newDate: { valueOf: () => any }) => {
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
