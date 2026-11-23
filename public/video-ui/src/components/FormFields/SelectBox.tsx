import React from 'react';
import FieldNotification from '../../constants/FieldNotification';

const pleaseSelect = 'Please select...';

type SelectValue = {
  id: string | number;
  title: string;
};

type Props = {
  displayDefault?: boolean;
  fieldValue: string;
  defaultOption?: string;
  selectValues: SelectValue[];
  editable: boolean;
  displayPlaceholder: (placeholder: string, fieldValue: string) => boolean;
  placeholder: string;
  fieldName: string;
  hasError: (props: Props) => boolean;
  input?: React.SelectHTMLAttributes<HTMLSelectElement>;
  onUpdateField: (value: string) => void;
  notification: FieldNotification | null;
};

export default class SelectBox extends React.Component<Props> {
  renderDefaultOption = () => {
    if (this.props.displayDefault || !this.props.fieldValue) {
      return <option>{this.props.defaultOption || pleaseSelect}</option>;
    }
  };

  renderField = () => {
    const matchingValues = this.props.selectValues.filter(
      value =>
        this.props.fieldValue && value.id.toString() === this.props.fieldValue
    );

    if (!this.props.editable) {
      const displayValue = matchingValues.length
        ? matchingValues[0].title
        : this.props.fieldValue;

      const fieldClassName = () =>
        'details-list__field ' +
        (this.props.displayPlaceholder(
          this.props.placeholder,
          this.props.fieldValue
        )
          ? 'details-list__empty'
          : '');

      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className={fieldClassName()}>{displayValue}</p>
        </div>
      );
    }

    const hasError = this.props.hasError(this.props);

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <select
          {...this.props.input}
          className={
            'form__field form__field--select ' +
            (hasError ? 'form__field--error' : '')
          }
          value={this.props.fieldValue}
          onChange={e => {
            this.props.onUpdateField(e.target.value);
          }}
        >
          {this.props.fieldValue && matchingValues.length === 0 && (
            <option>{pleaseSelect}</option>
          )}
          {this.renderDefaultOption()}
          {this.props.selectValues.map(function (value) {
            return (
              <option value={value.id} key={value.id}>
                {value.title}
              </option>
            );
          })}
        </select>
        {hasError ? (
          <p className="form__message form__message--error">
            {this.props.notification?.message}
          </p>
        ) : (
          ''
        )}
      </div>
    );
  };

  render() {
    return <div>{this.renderField()}</div>;
  }
}
