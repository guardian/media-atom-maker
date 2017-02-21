export default function usage(state = [], action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE': {
      return action.usages || [];
    }
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE': {
      state.push(action.newPage.usage);
      return state;
    }
    default: {
      return state;
    }
  }
}

