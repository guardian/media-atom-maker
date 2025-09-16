export default function usage(state = {}, action) {
  switch (action.type) {
    case 'usage/addNewlyCreatedVideoUsage': {
      const videoId = action.payload.videoId;
      state[videoId] = {
        composerId: action.payload.composerId,
        usage: action.payload.usage
      };
      return Object.assign({}, state);
    }
    default: {
      return state;
    }
  }
}
