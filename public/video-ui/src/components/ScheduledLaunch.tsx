import React, { useState } from 'react';
import moment from 'moment';
import ReactTooltip from 'react-tooltip';
import DatePicker from './FormFields/DatePicker';
import Icon from './Icon';
import { ScheduleRecap } from './ScheduleRecap';
import { isAfter, isFutureDate, isSameOrAfter } from '../util/dateHelpers';
import { impossiblyDistantDate } from '../constants/dates';
import VideoUtils from '../util/video';
import type { Video } from '../services/VideosApi';

type DelayPublicationOption = 'scheduledLaunch' | 'embargo';

type ScheduledLaunchProps = {
  video: Video;
  saveVideo: (video: Video) => Promise<void>;
  videoEditOpen: boolean;
  hasPublishedVideoPageUsages: () => boolean;
};

export const ScheduledLaunch = ({
  video,
  saveVideo,
  videoEditOpen,
  hasPublishedVideoPageUsages
}: ScheduledLaunchProps) => {
  const embargoDate: number | undefined = VideoUtils.getEmbargo(video);
  const scheduledLaunchDate: number | undefined =
    VideoUtils.getScheduledLaunch(video);

  const [selectedDate, setSelectedDate] = useState<number | null>(null);

  const [showMenu, setShowMenu] = useState(false);
  const [selectedDelayPublicationOption, setSelectedDelayPublicationOption] =
    useState<DelayPublicationOption | null>(null);
  const [invalidDateError, setInvalidDateError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const validateDate = (date: number | null) => {
    if (!date) {
      return;
    }

    if (
      selectedDelayPublicationOption === 'embargo' &&
      scheduledLaunchDate &&
      isAfter(date, scheduledLaunchDate)
    ) {
      setInvalidDateError("Embargo can't be later than scheduled launch!");
    } else if (
      selectedDelayPublicationOption === 'scheduledLaunch' &&
      embargoDate &&
      !isSameOrAfter(date, embargoDate)
    ) {
      setInvalidDateError("Scheduled launch can't be earlier than embargo!");
    } else if (!isFutureDate(date)) {
      setInvalidDateError('Date must be in the future!');
    } else {
      setInvalidDateError(null);
    }
  };

  const handleSelectOption = (
    delayPublicationOption: DelayPublicationOption
  ) => {
    setSelectedDelayPublicationOption(delayPublicationOption);
    setShowMenu(false);
  };

  const handleIndefiniteEmbargoClick = async () => {
    setIsLoading(true);

    await saveVideo({
      ...video,
      contentChangeDetails: {
        ...video.contentChangeDetails,
        embargo: {
          ...video.contentChangeDetails.embargo,
          date: impossiblyDistantDate
        }
      }
    });

    reset();
  };

  const handleDateChange = (date: number | null) => {
    setSelectedDate(date);
    validateDate(date);
  };

  const saveDate = async () => {
    if (!selectedDelayPublicationOption || !selectedDate) {
      return;
    }

    setIsLoading(true);

    await saveVideo({
      ...video,
      contentChangeDetails: {
        ...video.contentChangeDetails,
        [selectedDelayPublicationOption]: {
          ...video.contentChangeDetails[selectedDelayPublicationOption],
          date: selectedDate
        }
      }
    });

    reset();
  };

  const removeDate = async (delayPublicationOption: DelayPublicationOption) => {
    setIsLoading(true);

    await saveVideo({
      ...video,
      contentChangeDetails: {
        ...video.contentChangeDetails,
        [delayPublicationOption]: null
      }
    });

    reset();
  };

  const getNoScheduleReason = () => {
    if (!video.title) {
      return 'You must add a title before scheduling';
    }

    if (hasPublishedVideoPageUsages()) {
      return 'The atom cannot be scheduled because it has a published video page';
    }

    return null;
  };

  const reset = () => {
    setSelectedDelayPublicationOption(null);
    setShowMenu(false);
    setSelectedDate(null);
    setIsLoading(false);
    setInvalidDateError(null);
  };

  const hasIndefiniteEmbargo = !!(
    embargoDate && moment(embargoDate).isSameOrAfter(impossiblyDistantDate)
  );
  const noScheduleReason = getNoScheduleReason();
  const canShowScheduleRecap =
    (scheduledLaunchDate || embargoDate) && !selectedDelayPublicationOption;
  const canRemoveDate =
    (selectedDelayPublicationOption === 'scheduledLaunch' &&
      scheduledLaunchDate) ||
    (selectedDelayPublicationOption === 'embargo' && embargoDate);

  return (
    <div className="flex-container topbar__scheduler">
      {canShowScheduleRecap && (
        <ScheduleRecap
          scheduledLaunch={scheduledLaunchDate}
          embargo={embargoDate}
          hasIndefiniteEmbargo={hasIndefiniteEmbargo}
        />
      )}

      {selectedDelayPublicationOption && (
        <div className="topbar__date-picker">
          <DatePicker
            editable={true}
            onUpdateField={handleDateChange}
            fieldValue={
              selectedDate ??
              (selectedDelayPublicationOption === 'scheduledLaunch'
                ? (scheduledLaunchDate ?? null)
                : (embargoDate ?? null))
            }
            placeholder={null}
            fieldName="Select a date and time"
            dayOnly={false}
          />
        </div>
      )}

      {selectedDelayPublicationOption && invalidDateError && (
        <span className="topbar__alert">{invalidDateError}</span>
      )}

      {!selectedDelayPublicationOption && (
        <div className="scheduleOptionsWrapper">
          <div>
            <button
              className="btn btn--list"
              onClick={() => setShowMenu(showOptions => !showOptions)}
              data-tip="Delay publication..."
            >
              <Icon icon="access_time" />
              <Icon icon="expand_more" />
            </button>
          </div>

          {showMenu && (
            <ul className="scheduleOptions">
              {!hasIndefiniteEmbargo && (
                <li>
                  <button
                    className="btn btn--list-item"
                    onClick={() => handleSelectOption('scheduledLaunch')}
                    disabled={
                      !video || videoEditOpen || !!noScheduleReason || isLoading
                    }
                    data-tip={noScheduleReason}
                  >
                    {scheduledLaunchDate ? 'Edit scheduled date' : 'Schedule'}
                  </button>
                  <ReactTooltip />
                </li>
              )}

              {!hasIndefiniteEmbargo && embargoDate && (
                <li>
                  <button
                    className="btn btn--list-item"
                    onClick={() => handleSelectOption('embargo')}
                    disabled={!video || videoEditOpen || isLoading}
                  >
                    Edit embargo
                  </button>
                </li>
              )}

              {!embargoDate && !scheduledLaunchDate && (
                <li>
                  <button
                    className="btn btn--list-item"
                    onClick={handleIndefiniteEmbargoClick}
                    disabled={!video || videoEditOpen || isLoading}
                  >
                    {isLoading ? 'Loading...' : 'Prevent publication'}
                  </button>
                </li>
              )}

              {hasIndefiniteEmbargo && (
                <li>
                  <button
                    className="btn btn--list-item"
                    onClick={() => removeDate('embargo')}
                    disabled={!video || videoEditOpen || isLoading}
                  >
                    {isLoading ? 'Loading...' : 'Remove indefinite embargo'}
                  </button>
                </li>
              )}
            </ul>
          )}
        </div>
      )}

      {selectedDelayPublicationOption && (
        <>
          <button
            className="button__secondary--confirm"
            onClick={saveDate}
            disabled={
              isLoading ||
              !!invalidDateError ||
              !selectedDelayPublicationOption ||
              !selectedDate ||
              isLoading
            }
          >
            {isLoading ? 'Loading...' : 'Save'}
          </button>
          {canRemoveDate && (
            <button
              className="button__secondary--remove"
              onClick={() => removeDate(selectedDelayPublicationOption)}
              disabled={isLoading}
            >
              {isLoading ? 'Loading...' : 'Remove'}
            </button>
          )}
          <button
            className="button__secondary--cancel"
            disabled={isLoading}
            onClick={reset}
          >
            Cancel
          </button>
        </>
      )}

      <ReactTooltip />
    </div>
  );
};
