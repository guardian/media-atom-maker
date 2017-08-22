const initialState = {
  data: {},
  totalUsages: 0,
  totalVideoPages: 0
};

export default function usage(state = initialState, action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE': {
      return action.usages || {};
    }
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE': {
      // usages are sorted creation date DESC, new usage goes to the top
      state.data.preview.video = [action.newPage, ...state.data.preview.video];
      state.totalUsages = state.totalUsages + 1;
      state.totalVideoPages = state.totalVideoPages + 1;
      return state;
    }
    case 'VIDEO_PAGE_UPDATE_POST_RECEIVE': {
      return action.updatedUsages;
    }
    default: {
      return state;
    }
  }
}
