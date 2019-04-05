import React from 'react';
import PropTypes from 'prop-types';
import {ManagedField, ManagedForm, ManagedSection} from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import DatePicker from '../FormFields/DatePicker';
import { isVideoPublished } from '../../util/isVideoPublished';

class ContentChangeDetails extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  getTextField = (path, caption) => (
    <ManagedField fieldLocation={path} name={caption} disabled={true}>
      <TextInput />
    </ManagedField>
  );

  getDateField = (path, caption) => (
    <ManagedField fieldLocation={path} name={caption} disabled={true}>
      <DatePicker />
    </ManagedField>
  );

  render() {
    const { video } = this.props;

    const isPublished = isVideoPublished(video);

    return (
      <ManagedForm data={video}>
        <ManagedSection>
          {this.getDateField("contentChangeDetails.created.date", "Created at")}
          {this.getDateField("contentChangeDetails.lastModified.date", "Last modified at")}
          {isPublished && this.getDateField("contentChangeDetails.published.date", "Last published at")}
        </ManagedSection>
        <ManagedSection>
          {this.getTextField("contentChangeDetails.created.user.email", "Created by")}
          {this.getTextField("contentChangeDetails.lastModified.user.email", "Last modified by")}
          {isPublished && this.getTextField("contentChangeDetails.published.user.email", "Last published by")}
        </ManagedSection>
      </ManagedForm>
    );
  }
}

export default ContentChangeDetails;
