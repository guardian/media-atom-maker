import React from 'react';

type Props = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fieldValue: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  editable: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fieldName: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  maxLength: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  notification: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  placeholder: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  displayPlaceholder: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  hasError: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  input: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onUpdateField: any;
};

export default class TextInput extends React.Component<Props> {
  // Not ideal, but fixes an obscure bug which was causing the cursor to scroll
  // to the bottom on character input
  shouldComponentUpdate(nextProps: Props) {
    return (
      this.props.fieldValue !== nextProps.fieldValue ||
      this.props.editable !== nextProps.editable ||
      this.props.fieldName !== nextProps.fieldName ||
      this.props.maxLength !== nextProps.maxLength ||
      this.props.notification !== nextProps.notification ||
      this.props.placeholder !== nextProps.placeholder
    );
  }

  renderField = () => {
    if (!this.props.editable) {
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p
            className={
              'details-list__field ' +
              (this.props.displayPlaceholder(
                this.props.placeholder,
                this.props.fieldValue
              )
                ? 'details-list__empty'
                : '')
            }
          >
            {' '}
            {this.props.fieldValue}
          </p>
        </div>
      );
    }

    const hasError = this.props.hasError(this.props);

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <input
          {...this.props.input}
          maxLength={this.props.maxLength || ''}
          className={'form__field ' + (hasError ? 'form__field--error' : '')}
          type="text"
          value={this.props.fieldValue}
          onChange={e => {
            this.props.onUpdateField(e.target.value);
          }}
        />
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
