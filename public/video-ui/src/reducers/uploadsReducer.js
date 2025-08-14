import _ from 'lodash';

export default function uploads(state = [], action) {
  switch (action.type) {
    case 'UPLOAD_STARTED': {
      const id = action.upload.metadata.version;

      if (!_.find(state, upload => upload.id === id)) {
        // add additional item if currently uploading file is not in the list
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
