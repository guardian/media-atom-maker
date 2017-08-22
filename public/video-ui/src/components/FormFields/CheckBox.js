import React from 'react';

export default class CheckBox extends React.Component {
  renderCheckbox() {
    const checked =
      this.props.fieldValue && this.props.fieldValue !== this.props.placeholder;

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
        />
      </div>
    );
  }

  render() {
    return (
      <div data-tip={this.props.tooltip}>
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
