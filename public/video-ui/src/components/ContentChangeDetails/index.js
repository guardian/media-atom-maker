import React from 'react';
import { ManagedField, ManagedForm } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';

class ContentChangeDetails extends React.Component {
  getCreatedByField = () => {
    return (
      <ManagedField
        fieldLocation="contentChangeDetails.created.user.email"
        name="Created by"
        disabled={true}
      >
        <TextInput />
      </ManagedField>
    );
  };

  getModifiedByField = () => {
    return (
      <ManagedField
        fieldLocation="contentChangeDetails.lastModified.user.email"
        name="Last modified by"
        disabled={true}
      >
        <TextInput />
      </ManagedField>
    );
  };

  render() {
    const { video } = this.props;

    if (!video || !video.id) {
      return null;
    }

    return (
      <div className="form__group">
        <ManagedForm updateData={() => console.log()}>
          {this.getCreatedByField(video)}
          {this.getModifiedByField(video)}
        </ManagedForm>
      </div>
    );
  }
}

export default ContentChangeDetails;
