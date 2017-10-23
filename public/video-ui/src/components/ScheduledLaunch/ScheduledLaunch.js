import React from 'react';
import PropTypes from 'prop-types';
import DatePicker from '../FormFields/DatePicker';
import moment from 'moment';
import Icon from '../Icon';

export default class ScheduledLaunch extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    saveVideo: PropTypes.func.isRequired,
    videoEditOpen: PropTypes.bool.isRequired
  };

  state = {
    selectedDate: null,
    showDatePicker: false,
    showScheduleButton: true
  };

  saveScheduledLaunch = () => {
    const video = this.props.video;
    this.props.saveVideo(Object.assign({}, video, { scheduledLaunchDate: this.state.selectedDate }));
    this.setState({ showDatePicker: false });
  }

  removeScheduledLaunch = () => {
    const video = this.props.video;
    this.props.saveVideo(Object.assign({}, video, { scheduledLaunchDate: null }));
    this.setState({ showDatePicker: false });
  }


  render() {
    const { video, video: { scheduledLaunchDate }, videoEditOpen } = this.props;
    const { selectedDate } = this.state;
    const showDatePicker = this.state.showDatePicker && !videoEditOpen;
    const isFutureDate = selectedDate && moment(selectedDate).isAfter(moment());
    return (
      <div className="flex-container topbar__scheduler">
        { scheduledLaunchDate && !showDatePicker && <span className="topbar__launch-label">Scheduled: {moment(scheduledLaunchDate).format('Do MMM YYYY HH:MM')}</span> }
        {
          showDatePicker &&
          <DatePicker
            editable={true}
            onUpdateField={(date) => this.setState({ selectedDate: date })}
            fieldValue={selectedDate}
            placeholder="Set a date..."
          />
        }
        { showDatePicker && selectedDate && !isFutureDate && 
          <span className="topbar__alert">Date must be in the future!</span>
        }
        {
          !showDatePicker &&
          <button
            className="btn"
            onClick={() => this.setState({ showDatePicker: true })}
            disabled={!video || videoEditOpen}
          >
            {scheduledLaunchDate ? 'Reschedule' : 'Schedule'}
          </button>
        }
        {
          showDatePicker &&
          <button
            className="button__secondary--confirm"
            onClick={() => this.saveScheduledLaunch()}
            disabled={!selectedDate || !isFutureDate}
          >
            Save
          </button>
        }
        {
          scheduledLaunchDate && showDatePicker &&
          <button className="button__secondary--remove" onClick={() => this.removeScheduledLaunch()}>
            Remove
          </button>
        }
        {
          showDatePicker &&
          <button className="button__secondary--cancel" onClick={() => this.setState({ showDatePicker: false })}>
            Cancel
          </button>
        }
      </div>
    );
  }
}
