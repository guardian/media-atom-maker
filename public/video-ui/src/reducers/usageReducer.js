export default function usage(state = [], action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE': {
      return action.usages || [];
    }
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE': {
      // usages are sorted creation date DESC, new usage goes to the top
      state.unshift(action.newPage);
      return state;
    }
    default: {
      return state;
    }
  }
}

