import React from 'react';

export default class SaveButton extends React.Component {

  render () {
    return (
       <button type="button" className="btn" onClick={this.props.onSaveClick}>Save</button>
    );
  }
}
