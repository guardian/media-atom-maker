import VideosApi from '../../services/VideosApi';
import Logger from '../../logger';
import moment from 'moment';

function requestVideo(id) {
  return {
    type: 'VIDEO_GET_REQUEST',
    id: id,
    receivedAt: Date.now()
  };
}

function receiveVideo(video) {
  return {
    type: 'VIDEO_GET_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

function errorReceivingVideo(error) {
  Logger.error(error);
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get video',
    error: error,
    receivedAt: Date.now()
  };
}

export function getVideo(id) {
  return dispatch => {
    dispatch(requestVideo(id));
    return VideosApi.fetchVideo(id)
      .then(res => {
        // We and downstream consumers expect the scheduled launch to be an integer, but our API provides a string representation
        const scheduledLaunch = res?.contentChangeDetails?.scheduledLaunch?.date;
        if (scheduledLaunch) {
          res.contentChangeDetails.scheduledLaunch.date = moment(scheduledLaunch).valueOf();
        }
        const embargo = res?.contentChangeDetails?.embargo?.date;
        if (embargo) {
          res.contentChangeDetails.embargo.date = moment(embargo).valueOf();
        }
        dispatch(receiveVideo(res));
      })
      .catch(error => dispatch(errorReceivingVideo(error)));
  };
}
