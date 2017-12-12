import React from 'react';
import PropTypes from 'prop-types';
import DatePicker from '../FormFields/DatePicker';
import Icon from '../Icon';
import ScheduleRecap from '../ScheduleRecap/ScheduleRecap';
import {
  isFutureDate,
  isSameOrAfter,
  isAfter
} from '../../util/dateHelpers';
import { impossiblyDistantDate }  from '../../constants/dates';
import datesProperties from '../../constants/datesProperties';

export default class ScheduledLaunch extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    saveVideo: PropTypes.func.isRequired,
    videoEditOpen: PropTypes.bool.isRequired
  };

  state = {
    selectedScheduleDate: null,
    selectedEmbargoDate: null,
    showDatePicker: false,
    propertyName: null,
    showScheduleButton: true,
    showScheduleOptions: false,
    invalidDateError: null
  };

  componentWillReceiveProps(nextProps) {
    const embargo =
      nextProps.video.contentChangeDetails.embargo &&
      nextProps.video.contentChangeDetails.embargo.date;
    const scheduledLaunch =
      nextProps.video.contentChangeDetails.scheduledLaunch &&
      nextProps.video.contentChangeDetails.scheduledLaunch.date;
    this.setState({ selectedScheduleDate: scheduledLaunch || embargo });
    this.setState({ selectedEmbargoDate: embargo || scheduledLaunch });
  }

  validateDate = (date, propertyName) => {
    if (!date) {
      this.setState({ invalidDateError: null });
      return;
    }

    const { video: { contentChangeDetails } } = this.props;
    const scheduledLaunch =
      contentChangeDetails &&
      contentChangeDetails.scheduledLaunch &&
      contentChangeDetails.scheduledLaunch.date;
    const embargo =
      contentChangeDetails &&
      contentChangeDetails.embargo &&
      contentChangeDetails.embargo.date;

    if (
      propertyName === datesProperties.selectedEmbargoDate &&
      scheduledLaunch &&
      isAfter(date, scheduledLaunch)
    ) {
      this.setState({
        invalidDateError: "Embargo can't be later than scheduled launch!"
      });
    } else if (
      propertyName === datesProperties.selectedScheduleDate &&
      embargo &&
      !isSameOrAfter(date, embargo)
    ) {
      this.setState({
        invalidDateError: "Scheduled launch can't be earlier than embargo!"
      });
    } else if (!isFutureDate(date)) {
      this.setState({ invalidDateError: 'Date must be in the future!' });
    } else {
      this.setState({ invalidDateError: null });
    };
  };

  onSelectOption = propertyName => {
    const date = this.state[propertyName];
    this.validateDate(date, propertyName);
    this.setState({
      showDatePicker: true,
      propertyName,
      showScheduleOptions: false
    });
  };

  preventPublication = () => {
    const video = this.props.video;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          embargo: Object.assign({}, video.contentChangeDetails.embargo, {
            date: impossiblyDistantDate
          })
        })
      })
    );
    this.setState({
      selectedEmbargoDate: impossiblyDistantDate,
      showScheduleOptions: false
    });
  };

  setDate = (date, propertyName) => {
    this.setState({ [propertyName]: date });
    this.validateDate(date, propertyName);
  };

  saveDate = propertyName => {
    const key = propertyName === datesProperties.selectedScheduleDate ? 'scheduledLaunch' : 'embargo';
    const video = this.props.video;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          [key]: Object.assign({}, video.contentChangeDetails[key], {
            date: this.state[propertyName]
          })
        })
      })
    );
    this.setState({ showDatePicker: false, [propertyName]: null });
  };

  removeDate = propertyName => {
    const key = propertyName === datesProperties.selectedScheduleDate ? 'scheduledLaunch' : 'embargo';
    const video = this.props.video;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          [key]: null
        })
      })
    );
    this.setState({ showDatePicker: false, [propertyName]: null });
  };

  /* Render functions */

  renderScheduleOptions = (video, videoEditOpen, scheduledLaunch, embargo) => {
    const hasPreventedPublication = embargo && embargo >= impossiblyDistantDate;
    return (
      <ul className="scheduleOptions">
        {!hasPreventedPublication && (
          <li>
            <button
              className="btn btn--list"
              onClick={() => this.onSelectOption(datesProperties.selectedScheduleDate)}
              disabled={!video || videoEditOpen}
            >
              {scheduledLaunch ? 'Edit scheduled date' : 'Schedule'}
            </button>
          </li>
        )}
        {!hasPreventedPublication && (
          <li>
            <button
              className="btn btn--list"
              onClick={() => this.onSelectOption(datesProperties.selectedEmbargoDate)}
              disabled={!video || videoEditOpen}
            >
              {embargo ? 'Edit embargo' : 'Embargo until...'}
            </button>
          </li>
        )}
        {!embargo &&
          !scheduledLaunch && (
            <li>
              <button
                className="btn btn--list"
                onClick={() => this.preventPublication()}
                disabled={!video || videoEditOpen}
              >
                Prevent publication
              </button>
            </li>
          )}
        {hasPreventedPublication && (
          <li>
            <button
              className="btn btn--list"
              onClick={() => {
                this.removeDate(datesProperties.selectedEmbargoDate);
                this.setState({ showScheduleOptions: false });
              }}
              disabled={!video || videoEditOpen}
            >
              Remove indefinite embargo
            </button>
          </li>
        )}
      </ul>
    );
  };

  renderAlert = invalidDateError =>
    invalidDateError && (
      <span className="topbar__alert">{invalidDateError}</span>
    );

  renderDatePicker = (showDatePicker, propertyName, selectedScheduleDate, selectedEmbargoDate) => (
    showDatePicker &&
    <DatePicker
      editable={true}
      onUpdateField={date => this.setDate(date, propertyName)}
      fieldValue={
        propertyName === datesProperties.selectedScheduleDate
          ? selectedScheduleDate
          : selectedEmbargoDate
      }
    />
  )

  renderSaveButton = (propertyName, selectedScheduleDate, selectedEmbargoDate, invalidDateError) => (
    <button
      className="button__secondary--confirm"
      onClick={() => this.saveDate(propertyName)}
      disabled={
        invalidDateError ||
        ((propertyName === datesProperties.selectedScheduleDate && !selectedScheduleDate) ||
          (propertyName === datesProperties.selectedEmbargoDate && !selectedEmbargoDate))
      }
    >
      Save
    </button>
  )

  renderRemoveButton = propertyName => (
    <button
      className="button__secondary--remove"
      onClick={() => this.removeDate(propertyName)}
    >
      Remove
    </button>
  )

  renderCancelButton = () => (
    <button
      className="button__secondary--cancel"
      onClick={() =>
        this.setState({
          showDatePicker: false
        })
      }
    >
      Cancel
    </button>
  )

  renderSchedulerButton = (showScheduleOptions) => (
    <button
      className="btn btn--list"
      onClick={() =>
        this.setState({
          showScheduleOptions: !showScheduleOptions,
          propertyName: null
        })
      }
    >
      <Icon icon="access_time" />
    </button>
  )

  render() {
    const {
      video,
      video: { contentChangeDetails },
      videoEditOpen,
      hasPublishedVideoUsages
    } = this.props;
    const {
      selectedScheduleDate,
      selectedEmbargoDate,
      showScheduleOptions,
      propertyName
    } = this.state;
    const showDatePicker = this.state.showDatePicker && !videoEditOpen;
    const invalidDateError = this.state.invalidDateError;
    const scheduledLaunch =
      contentChangeDetails &&
      contentChangeDetails.scheduledLaunch &&
      contentChangeDetails.scheduledLaunch.date;
    const embargo =
      contentChangeDetails &&
      contentChangeDetails.embargo &&
      contentChangeDetails.embargo.date;
    const hasPreventedPublication = embargo && embargo >= impossiblyDistantDate;

    return (
      <div className="flex-container topbar__scheduler">
        {(scheduledLaunch || embargo) &&
          !showDatePicker && (
            <ScheduleRecap
              scheduledLaunch={scheduledLaunch}
              embargo={embargo}
              hasPreventedPublication={hasPreventedPublication}
            />
          )}
        {this.renderDatePicker(showDatePicker, propertyName, selectedScheduleDate, selectedEmbargoDate)}
        {showDatePicker && this.renderAlert(invalidDateError)}
        {!hasPublishedVideoUsages() &&
          !showDatePicker && (
            <div className="scheduleOptionsWrapper">
            {this.renderSchedulerButton(showScheduleOptions)}
              {showScheduleOptions &&
                this.renderScheduleOptions(
                  video,
                  videoEditOpen,
                  scheduledLaunch,
                  embargo
                )}
            </div>
          )}
        {showDatePicker && this.renderSaveButton(propertyName, selectedScheduleDate, selectedEmbargoDate, invalidDateError)}
        {((propertyName === datesProperties.selectedScheduleDate && scheduledLaunch) ||
          (propertyName === datesProperties.selectedEmbargoDate && embargo)) &&
          showDatePicker && this.renderRemoveButton(propertyName)}
        {showDatePicker && this.renderCancelButton()}
      </div>
    );
  }
}
