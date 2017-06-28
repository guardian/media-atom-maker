import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import { channelAllowed } from '../../util/channelAllowed';

export default class YoutubeMetaData extends React.Component {
  hasYouTubeUploads = () =>
    this.props.assets.some(asset => asset.platform === 'Youtube');

  notOnAllowedChannel = () =>
    !channelAllowed(this.props.video, this.props.youtube.channels);

  renderProjectIdForm() {
    return (
      <div>
        <ManagedForm
          data={this.props.video}
          updateData={this.props.saveVideo}
          editable={
            this.props.editable &&
              (!this.hasYouTubeUploads() ||
                (!this.props.video.plutoData ||
                  !this.props.video.plutoData.projectId))
          }
          updateErrors={this.props.updateErrors}
          formName={this.props.formName}
        >
          <ManagedField
            fieldLocation="plutoData.projectId"
            name="Pluto Project"
            isRequired={false}
            disabled={this.notOnAllowedChannel()}
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
        editable={
          this.props.editable &&
            (!this.hasYouTubeUploads() || !this.props.video.channelId)
        }
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
        editable={this.props.editable}
      >
        <ManagedField
          fieldLocation="youtubeCategoryId"
          name="YouTube Category"
          disabled={this.notOnAllowedChannel()}
        >
          <SelectBox selectValues={this.props.youtube.categories} />
        </ManagedField>
      </ManagedForm>
    );
  }

  render() {
    return (
      <div>
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            YouTube Data
          </header>
        </div>
        <div className="form__group">
          {this.renderChannelIdForm()}
          {this.renderProjectIdForm()}
          {this.renderCategoryForm()}
        </div>
      </div>
    );
  }
}
