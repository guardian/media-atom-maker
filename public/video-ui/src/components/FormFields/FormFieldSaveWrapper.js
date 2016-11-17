import React from 'react';
import SaveButton from '../utils/SaveButton';

export default class FormFieldSaveWrapper extends React.Component {

  state = {
    editable: false
  };

  toggleEditing = () => {
    if(this.state.editable) {
      this.setState({
        editable: false
      })
    } else {
      this.setState({
        editable: true
      })
    }
  };

  saveVideo = () => {
    this.props.saveVideo();
    this.toggleEditing();
  };

  resetVideo = () => {
    this.props.resetVideo();
    this.toggleEditing();
  };

  render() {
    console.log(this.props);
    return (
      <div className="form__save-wrapper">
        {this.props.children}
        {(this.state.editable ? "EDITING" : "NOT EDITING")}

        {(!this.state.editable ? <button type="button" className="btn" onClick={this.toggleEditing}>Edit</button>: '')}

        {(this.state.editable ? <SaveButton saveState={this.props.saveState} onSaveClick={this.saveVideo} onResetClick={this.props.resetVideo} /> : '' )}
      </div>
    )
  }
}
