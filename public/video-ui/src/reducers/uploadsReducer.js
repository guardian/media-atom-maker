export default function upload(state = null, action) {
  switch(action.type) {
    case 'UPLOAD_POLICY_RECEIVE':
      return { file: action.file, policy: action.policy };

    case 'UPLOAD_POLICY_CLEAR':
      return null;

    case 'START_UPLOAD':
      return { progress: 0.0 };

    case 'UPLOAD_PROGRESS':
      return { progress: (action.completed * 1.0) / action.total };

    case 'UPLOAD_COMPLETE':
      return { complete: true };

    default:
      return state;
  }
}
