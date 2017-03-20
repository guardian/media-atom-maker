export default function localUpload(state = { handle: null, progress: 0, total: 0 }, action) {
  switch(action.type) {
    case 'UPLOAD_STARTED': {
      const total = action.upload.parts[action.upload.parts.length - 1].end;
      return Object.assign({}, state, { created: action.receivedAt, handle: action.handle, total: total });
    }

    case 'UPLOAD_PROGRESS':
      return Object.assign({}, state, { progress: action.progress });

    default:
      return state;
  }
}