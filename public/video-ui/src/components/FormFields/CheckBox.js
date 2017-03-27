import React from 'react';

export default class CheckBox extends React.Component {

  renderCheckbox() {
    return (
      <div>
        <input
          id={this.props.fieldLocation}
          type="checkbox"
          disabled={!this.props.editable}
          checked= {this.props.fieldValue}
          onChange={(e) => {this.props.onUpdateField(e.target.checked)}}
        />
      </div>
    );
  }

  render(){
    return(
      <div>
        <p className="details-list__title">{this.props.fieldName}</p>
        <div className="details-list__labeled-filter">
          {this.renderCheckbox()}
          <p className="details-list__field details-list__labeled-filter__label">This content involves active criminal proceedings.</p>
        </div>
      </div>
    );
  }

}
