import VideosApi from '../../services/VideosApi';
import Logger from '../../logger';

function requestAudits(id) {
  return {
    type: 'AUDITS_GET_REQUEST',
    id: id,
    receivedAt: Date.now()
  };
}

function receiveAudits(audits) {
  return {
    type: 'AUDITS_GET_RECEIVE',
    audits: audits,
    receivedAt: Date.now()
  };
}

function errorReceivingAudits(error) {
  Logger.error(error);
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get audits',
    error: error,
    receivedAt: Date.now()
  };
}

export function getAudits(id) {
  return dispatch => {
    dispatch(requestAudits(id));
    return VideosApi.fetchAudits(id)
      .then(res => dispatch(receiveAudits(res)))
      .catch(error => dispatch(errorReceivingAudits(error)));
  };
}
