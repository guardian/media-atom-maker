export default function usage(state = {}, action) {
  switch (action.type) {
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE': {
      const videoId = action.newPage.videoId;
      state[videoId] = {
        composerId: action.newPage.composerId,
        usage: action.newPage.usage
      };
      return Object.assign({}, state);
    }

    default: {
      return state;
    }
  }
}
