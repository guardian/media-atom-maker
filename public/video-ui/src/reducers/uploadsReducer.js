import _ from 'lodash';

export default function uploads(state = [], action) {
  switch (action.type) {
    case 'UPLOAD_STARTED': {
      const id = action.upload.id;

      if (!_.find(state, upload => upload.id === id)) {
        const status = {
          id,
          failed: false,
          processing: { status: 'Uploading' }
        };

        return [status, ...state];
      }

      return state;
    }

    case 'RUNNING_UPLOADS':
      return action.uploads;

    default:
      return state;
  }
}
