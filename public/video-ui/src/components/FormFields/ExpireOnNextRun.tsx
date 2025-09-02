import React from 'react';

type EditorProps = {
  fieldValue: string;
  onUpdateField: (string: string|number) => any;
  editable: boolean;
  fieldLocation: string;
  fieldName: string;
}


export class ExpireOnNextRun extends React.Component<EditorProps> {

  updateValueExpiryDate = () => {
    const now = new Date();
    const minutes = now.getMinutes();
    const newDate= new Date().setMinutes(Math.ceil(minutes/15)*15); // round to next quarter of hour
    this.props.onUpdateField( newDate);
  };

  renderExpireOnNextRunButton = () => {
    if (!this.props.editable) {
      return null;
    }

    return (
      <button
        type="button"
        title={"This will set the expiry date to the next run of the process, which runs every 15 minutes."}
        disabled={!this.props.editable}
        className="btn form__label__button form__label__copy-button"
        onClick={this.updateValueExpiryDate}
        data-tip="Set Expiry Date to Next Run of the process"
        data-place="top"
      >
        Expire On Next Run
      </button>

    );
  }
  render() {
    return (
      <div className="form__row">
        <div className="form__label__layout form__label__layout__rich-text">
          {this.renderExpireOnNextRunButton()}
        </div>
      </div>
    );
  }
}
