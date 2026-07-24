import React from 'react';

type Props = {
  fieldValue: any;
  placeholder: any;
  fieldId?: string;
  fieldLocation: any;
  editable: any;
  onUpdateField: any;
  tooltip: any;
  fieldName: any;
  fieldDetails: any;
};

export default class CheckBox extends React.Component<Props> {
  renderCheckbox() {
    const checked =
      !!this.props.fieldValue &&
      this.props.fieldValue !== this.props.placeholder;

    return (
      <div>
        <input
          id={this.props.fieldId || this.props.fieldLocation}
          type="checkbox"
          disabled={!this.props.editable}
          checked={checked}
          onChange={e => {
            this.props.onUpdateField(e.target.checked);
          }}
          className="form-checkbox"
        />
      </div>
    );
  }

  render() {
    return (
      <div
        data-tip={this.props.editable ? this.props.tooltip : ''}
        data-place="top"
      >
        <p className="details-list__title">{this.props.fieldName}</p>
        <div className="details-list__labeled-filter">
          {this.renderCheckbox()}
          <p className="details-list__field details-list__labeled-filter__label">
            {this.props.fieldDetails}
          </p>
        </div>
      </div>
    );
  }
}
