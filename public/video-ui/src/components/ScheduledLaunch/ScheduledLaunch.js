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
    this.props.saveVideo(Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          scheduledLaunch: Object.assign({}, video.contentChangeDetails.scheduledLaunch, {
            date: this.state.selectedDate
          })
        })
      })
    );
    this.setState({ showDatePicker: false });
  }

  removeScheduledLaunch = () => {
    const video = this.props.video;
      this.props.saveVideo(Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          scheduledLaunch: null
        })
      })
    );
    this.setState({ showDatePicker: false });
  }


  render() {
    const { video, video: { contentChangeDetails }, videoEditOpen } = this.props;
    const { selectedDate } = this.state;
    const showDatePicker = this.state.showDatePicker && !videoEditOpen;
    const isFutureDate = selectedDate && moment(selectedDate).isAfter(moment());
    const scheduledLaunch = contentChangeDetails && contentChangeDetails.scheduledLaunch && contentChangeDetails.scheduledLaunch.date;
    return (
      <div className="flex-container topbar__scheduler">
        {scheduledLaunch && !showDatePicker && <span className="topbar__launch-label">Scheduled: {moment(scheduledLaunch).format('Do MMM YYYY HH:mm')}</span> }
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
            {scheduledLaunch ? 'Reschedule' : 'Schedule'}
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
          scheduledLaunch && showDatePicker &&
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
