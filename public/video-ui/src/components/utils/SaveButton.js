import React from 'react';

export default class SaveButton extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {
    return (
       <button className="btn" onClick={this.props.onSaveClick}>Save</button>
    );
  }
}
