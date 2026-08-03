import React from 'react';
import moment from 'moment';

type ScheduleRecapProps = {
  scheduledLaunch?: number | string | null;
  embargo?: number | string | null;
  hasIndefiniteEmbargo?: boolean;
};

export const ScheduleRecap = ({
  scheduledLaunch,
  embargo,
  hasIndefiniteEmbargo
}: ScheduleRecapProps) => (
  <div className="topbar__launch-label">
    {hasIndefiniteEmbargo ? (
      <span className="scheduledSummary--embargo">Embargoed indefinitely</span>
    ) : (
      <>
        <div>
          <span className="scheduledSummary--scheduledLaunch">Scheduled: </span>
          {scheduledLaunch
            ? moment(scheduledLaunch).format('Do MMM YYYY HH:mm')
            : '—'}
        </div>
        <div>
          <span className="scheduledSummary--embargo">Embargoed: </span>
          {embargo ? moment(embargo).format('Do MMM YYYY HH:mm') : '—'}
        </div>
      </>
    )}
  </div>
);
