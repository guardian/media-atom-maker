import React from 'react';

interface ExpireNowComponentProps {
  fieldValue: string;
  onUpdateField: (string: number) => void;
  editable: boolean;
  fieldName: string;
}

export const ExpireNowComponent = (props: ExpireNowComponentProps ) => {

  const updateValueExpiryDate = (props: ExpireNowComponentProps) => {
    const now = new Date();
    const minutes = now.getMinutes();
    const newDate= new Date();
    newDate.setMinutes(Math.ceil(minutes/15) * 15); // round to next quarter of hour
    newDate.setSeconds(0);
    newDate.setMilliseconds(0);
    props.onUpdateField(newDate.getTime());
  };
  return (
    <div className="form__row">
      <div className="form__label__layout form__label__layout__rich-text">
        {props.editable && (
          <div className ="form__label__layout__expire-now-button">
            <button
              type="button"
              disabled={!props.editable}
              className="btn form__label__button form__label__copy-button"
              onClick={() => updateValueExpiryDate(props)}
              data-tip="Set Expiry Date to Next Run of the process"
              data-place="top"
            >
              Expire Now
            </button>
            <p className="form__message form__message--warning">
              This will set the expiry date to the next run of the process, which runs regularly.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

