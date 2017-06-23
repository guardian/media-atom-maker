import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';

export default class YoutubeMetaData extends React.Component {
  hasYouTubeUploads = () =>
    this.props.assets.some(asset => asset.platform === 'Youtube');

  renderProjectIdForm() {
    return (
      <div>
        <ManagedForm
          data={this.props.video}
          updateData={this.props.saveVideo}
          editable={
            !this.hasYouTubeUploads() ||
              (!this.props.video.plutoData ||
                !this.props.video.plutoData.projectId)
          }
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
      </div>
    );
  }

  renderChannelIdForm() {
    return (
      <ManagedForm
        data={this.props.video}
        updateData={this.props.saveVideo}
        editable={!this.hasYouTubeUploads() || !this.props.video.channelId}
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
        {this.renderProjectIdForm()}
        {this.renderCategoryForm()}
      </div>
    );
  }
}
