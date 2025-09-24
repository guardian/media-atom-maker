import React, { useState, useEffect, useCallback } from 'react';
import {
  durationToMinAndSecs,
  secondsToDurationStr
} from '../../util/durationHelpers';

const getStateFromProps = props => {
  const dur = props.rawFieldValue || 0;
  const { mins, secs } = durationToMinAndSecs(dur);
  return {
    mins: `${mins}`,
    secs: `${secs}`
  };
};

const DurationInput = props => {
  const isLive = props.rawFieldValue === 0;

  const [state, setState] = useState(() => getStateFromProps(props));

  useEffect(() => {
    setState(getStateFromProps(props));
  }, [props.rawFieldValue]);

  const updateDuration = useCallback(
    (mins, secs) => {
      const minsInt = parseInt(mins ?? state.mins, 10);
      const secsInt = parseInt(secs ?? state.secs, 10);
      props.onUpdateField(minsInt * 60 + secsInt);
    },
    [state.mins, state.secs, props]
  );

  const updateMins = mins => {
    setState(prevState => ({
      ...prevState,
      mins
    }));
    updateDuration(mins);
  };

  const updateSecs = secs => {
    const newSecs = secs ? `${Math.min(parseInt(secs, 10), 59)}` : '0';
    setState(prevState => ({
      ...prevState,
      secs: newSecs
    }));
    updateDuration(undefined, newSecs);
  };

  if (!props.editable) {
    return (
      <div>
        <div>
          <p className="details-list__title">{props.fieldName}</p>
          {isLive ? (
            <p className="details-list__field">Live video – zero duration</p>
          ) : (
            <>
              <p
                className={
                  'details-list__field ' +
                  (props.displayPlaceholder(props.placeholder, props.fieldValue)
                    ? 'details-list__empty'
                    : '')
                }
              >
                {' '}
                {secondsToDurationStr(props.rawFieldValue)}
              </p>
            </>
          )}
        </div>
      </div>
    );
  }

  const hasError = props.hasError(props);

  return (
    <div>
      <div className="form__row">
        <label className="form__label">{props.fieldName}</label>

        <div>
          <div className="details-list__labeled-filter">
            <div>
              <input
                id={props.fieldId || props.fieldLocation}
                type="checkbox"
                disabled={!props.editable}
                checked={isLive}
                onChange={e => {
                  props.onUpdateField(e.target.checked ? 0 : 1);
                }}
                className="form-checkbox"
              />
            </div>
            <p className="details-list__field details-list__labeled-filter__label">
              Live video
            </p>
          </div>
        </div>

        <>
          <div className="form__description">
            <em>
              Use this to edit videos where the duration is being reported
              incorrectly (i.e. 0:00)
            </em>
          </div>
          <div className={isLive ? 'form-element--hidden' : ''}>
            <input
              type="text"
              size="3"
              className={
                'form__field form__field--inline ' +
                (hasError ? 'form__field--error' : '')
              }
              value={state.mins}
              disabled={isLive}
              onChange={e => {
                updateMins(e.target.value);
              }}
            />
            <span style={{ margin: '0 5px' }}>mins</span>
            <input
              type="text"
              size="3"
              className={
                'form__field form__field--inline ' +
                (hasError ? 'form__field--error' : '')
              }
              value={state.secs}
              disabled={isLive}
              onChange={e => {
                updateSecs(e.target.value);
              }}
            />
            <span style={{ margin: '0 5px' }}>secs</span>
          </div>
        </>

        {hasError ? (
          <p className="form__message form__message--error">
            {props.notification.message}
          </p>
        ) : (
          ''
        )}
      </div>
    </div>
  );
};

export default React.memo(DurationInput);
