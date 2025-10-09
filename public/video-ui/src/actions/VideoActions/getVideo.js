import VideosApi from '../../services/VideosApi';
import moment from 'moment';
import { showError } from '../../slices/error';
import {setVideo, setSaving} from '../../slices/video';

export function getVideo(id) {
  return dispatch => {
    dispatch(setSaving(true));
    return VideosApi.fetchVideo(id)
      .then(res => {
        // We and downstream consumers expect the scheduled launch to be an integer, but our API provides a string representation
        const scheduledLaunch =
          res?.contentChangeDetails?.scheduledLaunch?.date;
        if (scheduledLaunch) {
          res.contentChangeDetails.scheduledLaunch.date =
            moment(scheduledLaunch).valueOf();
        }
        const embargo = res?.contentChangeDetails?.embargo?.date;
        if (embargo) {
          res.contentChangeDetails.embargo.date = moment(embargo).valueOf();
        }
        const expiry = res?.contentChangeDetails?.expiry?.date;
        if (expiry) {
          res.contentChangeDetails.expiry.date = moment(expiry).valueOf();
        }
        dispatch(setSaving(false));
        dispatch(setVideo(res));
      })
      .catch(error => dispatch(showError('Could not get video', error)));
  };
}
