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
