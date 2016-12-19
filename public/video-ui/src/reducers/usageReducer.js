export default function usage(state = {}, action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE':
      return action.usages || {};
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE':
      const videoId = action.newPage.videoId;
      if (state[videoId]) {
        state[videoId].push(action.newPage.usage);
      } else {
        state[videoId] = [action.newPage.usage];
      }
      return state;
    default:
      return state;
  }
}

