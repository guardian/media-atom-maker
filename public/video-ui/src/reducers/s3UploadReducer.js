const EMPTY = { id: null, progress: 0, total: 0 };

// This key covers the first part of a video upload where we put the video into S3 from the client browser.
// We hold that progress in a separate state key from the progress of uploading to YouTube on the server side.
// This way we can give a finer grained progress bar to the user for the S3 upload.
export default function s3Upload(state = EMPTY, action) {
  switch (action.type) {
    case 'UPLOAD_STARTED': {
      const total = action.upload.parts[action.upload.parts.length - 1].end;
      return Object.assign({}, state, {
        id: action.upload.id,
        total: total
      });
    }

    case 'UPLOAD_PROGRESS':
      return Object.assign({}, state, { progress: action.progress });

    case 'UPLOAD_COMPLETE':
      return EMPTY;

    default:
      return state;
  }
}
