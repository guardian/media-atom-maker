import React from 'react';
import PropTypes from 'prop-types';
import { ManagedField, ManagedForm } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';

class ContentChangeDetails extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

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
      <ManagedForm data={video}>
        {this.getCreatedByField()}
        {this.getModifiedByField()}
      </ManagedForm>
    );
  }
}

export default ContentChangeDetails;
