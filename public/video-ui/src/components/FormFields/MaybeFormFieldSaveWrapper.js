import React from 'react';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import {Field} from 'redux-form';

export default class MaybeFormFieldSaveWrapper extends React.Component {

  render() {
    if (this.props.disableEditing) {
      return (
        <Field
          name={this.props.name}
          type={this.props.text}
          component={this.props.component}
          video={this.props.video}
          updateVideo={this.props.updateVideo}
          editable={this.props.editable}/>
      );
    }
    return (
      <FormFieldSaveWrapper
        saveVideo={this.props.saveVideo}
        resetVideo={this.props.resetVideo}
        editable={this.props.editable}
        saveState={this.props.saveState}>
        <Field
          name={this.props.name}
          type={this.props.text}
          component={this.props.component}
          video={this.props.video}
          updateVideo={this.props.updateVideo}
          editable={this.props.editable}
        />
      </FormFieldSaveWrapper>
    );
  }
}
