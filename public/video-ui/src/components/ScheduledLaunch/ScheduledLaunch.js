import React from 'react';
import PropTypes from 'prop-types';
import Icon from '../Icon';
import { ManagedForm, ManagedField } from '../ManagedForm';
import DatePicker from '../FormFields/DatePicker';

export default class ScheduledLaunch extends React.Component {

  static propTypes = {
    video: PropTypes.object.isRequired,
    updateVideo: PropTypes.func.isRequired,
    saveVideo: PropTypes.func.isRequired,
  }

  scheduleAtom() {
    console.log('scheduling atom!');
  }

  renderScheduledLaunchPicker() {

    return (
      <ManagedForm
        data={this.props.video}
        editable={true}
        updateData={(e) => this.props.updateVideo(e)}
      >
        <ManagedField fieldLocation="scheduledLaunch" name="">
          <DatePicker />
        </ManagedField>
      </ManagedForm>
    );
  }

  renderSetScheduledLaunchButton() {

  }

  render() {

    if (!this.props.video || !this.props.video.scheduledLaunchDate) {
      return (
        <div>
          <button
            onClick={this.scheduleAtom}
          >
            Schedule
          </button>
          { this.renderScheduledLaunchPicker() }
        </div>
      );
    }

    return (
      <div>
        Scheduled launch set
      </div>
    );
  }
}
