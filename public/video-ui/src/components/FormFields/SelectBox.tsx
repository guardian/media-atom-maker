import React from 'react';

const pleaseSelect = 'Please select...';

type Props = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  displayDefault: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fieldValue: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  defaultOption: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  selectValues: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  editable: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  displayPlaceholder: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  placeholder: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fieldName: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  hasError: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  input: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onUpdateField: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  notification: any;
};

export default class SelectBox extends React.Component<Props> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  hasError: any;
  getClassName = () => {
    return (
      'form__field form__field--select ' +
      (this.hasError() ? 'form__field--error' : '')
    );
  };

  renderDefaultOption = () => {
    if (this.props.displayDefault || !this.props.fieldValue) {
      return (
        // @ts-expect-error TS(2322): Type 'null' is not assignable to type 'string | nu... Remove this comment to see the full error message
        <option value={null}>{this.props.defaultOption || pleaseSelect}</option>
      );
    }
  };

  renderField = () => {
    const matchingValues = this.props.selectValues.filter(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (fieldValue: any) =>
        this.props.fieldValue &&
        fieldValue.id.toString() === this.props.fieldValue.toString()
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
            <option
              value={this.props.fieldValue.id}
              key={this.props.fieldValue.id}
            >
              {pleaseSelect}
            </option>
          )}
          {this.renderDefaultOption()}
          {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
          {this.props.selectValues.map(function (value: any) {
            return (
              <option value={value.id} key={value.id}>
                {value.title}
              </option>
            );
          })}
        </select>
        {hasError ? (
          <p className="form__message form__message--error">
            {this.props.notification.message}
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
