import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';

export default class YoutubeMetaData extends React.Component {
  render() {
    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateErrors={this.props.updateErrors}
          formName={this.props.formName}
        >
          <ManagedField
            fieldLocation="youtubeCategoryId"
            name="YouTube Category"
          >
            <SelectBox selectValues={this.props.youtube.categories} />
          </ManagedField>
          <ManagedField fieldLocation="channelId" name="YouTube Channel">
            <SelectBox selectValues={this.props.youtube.channels} />
          </ManagedField>
          <ManagedField
            fieldLocation="plutoData.projectId"
            name="Pluto Project"
            isRequired={false}
          >
            <SelectBox selectValues={this.props.pluto.projects} />
          </ManagedField>
        </ManagedForm>
      </div>
    );
  }
}
