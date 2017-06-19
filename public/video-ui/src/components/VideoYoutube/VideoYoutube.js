import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';

export default class YoutubeMetaData extends React.Component {
  renderProjectId() {
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
          placeholder={
            this.props.pluto && this.props.pluto.projects
              ? ''
              : 'Pluto projects are currently unavailable'
          }
        >
          <SelectBox
            selectValues={this.props.pluto ? this.props.pluto.projects : []}
          />
        </ManagedField>
      </ManagedForm>
    );
  }

  render() {
    return (
      <div className="form__group">
        <div>
          <p className="details-list__title">Channel Id</p>
          <p className="details-list__field">{this.props.video.channelId}</p>
        </div>
        {this.renderProjectId()}
      </div>
    );
  }
}
