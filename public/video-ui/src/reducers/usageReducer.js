export default function usage(state = {}, action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE':
      return action.usages || {};
    default:
      return state;
  }
}

