import React from 'react';
import moment from 'moment';
import PropTypes from 'prop-types';

const ScheduleRecap = ({ scheduledLaunch, embargo, hasPreventedPublication }) => (
  !hasPreventedPublication ?
  <div className="topbar__launch-label">
    <div>
      <span className="scheduledSummary--scheduledLaunch">
        {'Scheduled: '}
      </span>
      {scheduledLaunch
        ? moment(scheduledLaunch).format('Do MMM YYYY HH:mm')
        : '-'}
    </div>
    <div>
      <span className="scheduledSummary--embargo">
        {'Embargoed: '}
      </span>
      {embargo ? moment(embargo).format('Do MMM YYYY HH:mm') : '-'}
    </div>
  </div>
  :
  <div className="topbar__launch-label">
    <span className="scheduledSummary--embargo">
      Embargoed indefinitely
    </span>
  </div>
);

ScheduleRecap.propTypes = {
  scheduledLaunch: PropTypes.number,
  embargo: PropTypes.number,
  hasPreventedPublication: PropTypes.bool
};

export default ScheduleRecap;
