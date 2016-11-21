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

  fieldWithState = () => {
    return React.cloneElement(this.props.children, {editable: this.state.editable});
  };

  render() {
    return (
      <div className="form__save-wrapper">
        {this.fieldWithState()}

        {(!this.state.editable ? <button type="button" className="form__edit-btn" onClick={this.toggleEditing}>Edit</button> : false)}

        {(this.state.editable ? <SaveButton saveState={this.props.saveState} onSaveClick={this.saveVideo} onResetClick={this.resetVideo} /> : '' )}
      </div>
    )
  }
}
