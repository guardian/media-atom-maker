export default function usage(state = {}, action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE': {
      return action.usages || {};
    }
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE': {
      // usages are sorted creation date DESC, new usage goes to the top
      state.preview.video = [action.newPage, ...state.preview.video];
      return state;
    }
    case 'VIDEO_PAGE_UPDATE_POST_RECEIVE': {
      Object.keys(action.updatedUsages).forEach(contentState => {
        state[contentState] = Object.assign({}, state[contentState], {
          video: action.updatedUsages[contentState]
        });
      });

      return state;
    }
    default: {
      return state;
    }
  }
}
