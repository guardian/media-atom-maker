export default function usage(state = [], action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE': {
      return action.usages || {};
    }
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE': {
      // usages are sorted creation date DESC, new usage goes to the top
      state.unshift(action.newPage);
      return state;
    }
    case 'VIDEO_PAGE_UPDATE_POST_RECEIVE': {
      const newState = state.map(usage => {
        usage.fields.headline = action.newTitle;
        return usage;
      });

      return newState;
    }
    default: {
      return state;
    }
  }
}
