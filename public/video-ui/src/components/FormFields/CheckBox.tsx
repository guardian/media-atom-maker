import React from 'react';

type CheckBoxProps = {
  fieldId?: string;
  fieldLocation?: string;
  editable?: boolean;
  fieldValue?: unknown;
  placeholder?: unknown;
  onUpdateField: (value: boolean) => void | Promise<unknown>;
  fieldName?: string;
  fieldDetails?: React.ReactNode;
  tooltip?: string;
};

export default class CheckBox extends React.Component<CheckBoxProps> {
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
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
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
