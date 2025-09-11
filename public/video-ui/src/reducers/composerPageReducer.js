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

    case 'RECEIVE_COMPOSER_PATH_REPORT': {

      // TO DO - if the action.composerID matches the current page, set some state somewhere with the path report
      console.log(action);
      console.log('state',state);
      return state;
    }

    default: {
      return state;
    }
  }
}
