const EMPTY = { handle: null, progress: 0, total: 0 };

export default function localUpload(state = EMPTY, action) {
  switch(action.type) {
    case 'UPLOAD_STARTED': {
      const total = action.upload.parts[action.upload.parts.length - 1].end;
      return Object.assign({}, state, { created: action.receivedAt, handle: action.handle, total: total });
    }

    case 'UPLOAD_PROGRESS':
      return Object.assign({}, state, { progress: action.progress });

    case 'UPLOAD_COMPLETE':
      return EMPTY;

    default:
      return state;
  }
}