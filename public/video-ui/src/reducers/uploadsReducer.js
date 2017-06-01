import _ from 'lodash';

export default function uploads(state = [], action) {
  const id = action.upload.id;

  switch (action.type) {
    case 'UPLOAD_STARTED':
      if (!_.find(state, upload => upload.id === id)) {
        const status = { id: id, status: 'Uploading', failed: false };
        return [status, ...state];
      }

      return state;

    case 'RUNNING_UPLOADS':
      return action.uploads;

    default:
      return state;
  }
}
