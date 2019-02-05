import React from 'react';
import {
  durationToMinAndSecs,
  secondsToDurationStr
} from '../../util/durationHelpers';

const getStateFromProps = props => {
  const dur = props.fieldValue || 0;

  const { mins, secs } = durationToMinAndSecs(dur);

  return {
    mins: `${mins}`,
    secs: `${secs}`
  };
};

export default class DurationInput extends React.Component {
  constructor(props) {
    super(props);
    this.state = getStateFromProps(this.props);
  }

  componentWillReceiveProps(props) {
    this.setState(getStateFromProps(props));
  }

  updateDuration() {
    const minsInt = parseInt(this.state.mins, 10);
    const secsInt = parseInt(this.state.secs || '0', 10);
    this.props.onUpdateField(minsInt * 60 + secsInt);
  }

  updateMins(mins) {
    this.setState(
      {
        mins
      },
      () => this.updateDuration()
    );
  }

  updateSecs(secs) {
    this.setState(
      {
        secs: secs ? `${Math.min(parseInt(secs, 10), 59)}` : '' // limit seconds to 59
      },
      () => this.updateDuration()
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
            {secondsToDurationStr(this.props.fieldValue)}
          </p>
        </div>
      );
    }

    const hasError = this.props.hasError(this.props);

    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <span className="form__description">
          <em>
            Use this to edit videos where the duration is being reported
            incorrectly (i.e. 0:00)
          </em>
        </span>
        <input
          type="text"
          size="3"
          className={'form__field form__field--inline ' + (hasError ? 'form__field--error' : '')}
          value={this.state.mins}
          onChange={e => {
            this.updateMins(e.target.value);
          }}
        />
        <span style={{ margin: '0 5px' }}>mins</span>
        <input
          type="text"
          size="3"
          className={'form__field form__field--inline ' + (hasError ? 'form__field--error' : '')}
          value={this.state.secs}
          onChange={e => {
            this.updateSecs(e.target.value);
          }}
        />
        <span style={{ margin: '0 5px' }}>secs</span>
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
