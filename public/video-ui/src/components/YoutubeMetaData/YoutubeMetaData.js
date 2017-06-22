import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';

export default class YoutubeMetaData extends React.Component {
  renderProjectIdForm() {
    if (
      !this.props.video.plutoData &&
      (!this.props.pluto || !this.props.pluto.projects)
    ) {
      return (
        <div>
          <p className="details-list__title">Pluto Project</p>
          <p className="details-list__field">
            Pluto projects are currently unavailable
          </p>
        </div>
      );
    }
    return (
      <ManagedForm
        data={this.props.video}
        updateData={this.props.saveVideo}
        editable={this.props.editable}
        updateErrors={this.props.updateErrors}
        formName={this.props.formName}
      >
        <ManagedField
          fieldLocation="plutoData.projectId"
          name="Pluto Project"
          isRequired={false}
        >
          <SelectBox
            selectValues={this.props.pluto ? this.props.pluto.projects : []}
          />
        </ManagedField>
      </ManagedForm>
    );
  }

  renderChannelIdForm() {
    return (
      <ManagedForm
        data={this.props.video}
        updateData={this.props.saveVideo}
        editable={!this.props.video.channelId}
      >
        <ManagedField fieldLocation="channelId" name="YouTube Channel">
          <SelectBox selectValues={this.props.youtube.channels} />
        </ManagedField>
      </ManagedForm>
    );
  }

  renderCategoryForm() {
    return (
      <ManagedForm
        data={this.props.video}
        updateData={this.props.saveVideo}
        editable={true}
      >
        <ManagedField fieldLocation="youtubeCategoryId" name="YouTube Category">
          <SelectBox selectValues={this.props.youtube.categories} />
        </ManagedField>
      </ManagedForm>
    );
  }

  render() {
    return (
      <div className="form__group">
        {this.renderChannelIdForm()}
        {this.renderCategoryForm()}
        {this.renderProjectIdForm()}
      </div>
    );
  }
}
