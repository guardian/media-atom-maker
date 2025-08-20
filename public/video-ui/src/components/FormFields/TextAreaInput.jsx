import React from 'react';

export default class TextAreaInput extends React.Component {

  // Not ideal, but fixes an obscure bug which was causing the cursor to scroll
  // to the bottom on character input
  shouldComponentUpdate(nextProps) {
    return (
      this.props.fieldValue !== nextProps.fieldValue ||
      this.props.editable !== nextProps.editable ||
      this.props.fieldName !== nextProps.fieldName ||
      this.props.maxLength !== nextProps.maxLength ||
      this.props.rows !== nextProps.rows ||
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
                  : 'details-list__field--text-area')
            }
          >
            {this.props.fieldValue}
          </p>
        </div>
      );
    }

    const hasError = this.props.hasError(this.props);

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <textarea
          {...this.props.input}
          rows={this.props.rows || '5'}
          maxLength={this.props.maxLength || ''}
          className={'form__field form__field--textarea' + (hasError ? 'form__field--error' : '')}
          value={this.props.fieldValue}
          onChange={e => {
            this.props.onUpdateField(e.target.value);
          }}
        />
        {hasError
          ? <p className="form__message form__message--error">
              {this.props.notification.message}
            </p>
          : ''}
      </div>
    );
  };

  render() {
    return (
      <div>
        {this.renderField()}
      </div>
    );
  }
}
