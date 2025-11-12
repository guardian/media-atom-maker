import moment from 'moment';
import VideosApi from '../../services/VideosApi';
import { showError } from '../../slices/error';
import {
  fetchIconikCommissions,
  fetchIconikProjects,
  fetchIconikWorkingGroups
} from '../../slices/iconik';
import { setSaving, setVideo } from '../../slices/video';

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
        dispatch(fetchIconikWorkingGroups());
        if (res.iconikData?.workingGroupId) {
          dispatch(fetchIconikCommissions(res.iconikData.workingGroupId));
        }
        if (res.iconikData?.commissionId) {
          dispatch(fetchIconikProjects(res.iconikData.commissionId));
        }
      })
      .catch(error => dispatch(showError('Could not get video', error)));
  };
}
