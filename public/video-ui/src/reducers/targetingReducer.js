// null here means targets haven't been fetched, an empty array means they have
// been fetched but there are none
export default function targeting(
  state = { targets: null, deleting: [] },
  action
) {
  switch (action.type) {
    case 'TARGETING_UPDATE_REQUEST':
      return {
        ...state,
        targets: [
          ...state.targets.filter(({ id }) => id !== action.target.id),
          action.target
        ]
      };
    // when we start a new request for targets reset to our 'loading' state
    // this gets called when we look for targets for a new video
    case 'TARGETING_GET_REQUEST':
      return { targets: null, deleting: [] };
    // if we receive any targets, for now, completely overwrite what's in here
    case 'TARGETING_POST_RECEIVE':
    // v--- fallthrough ---v
    case 'TARGETING_GET_RECEIVE':
      return {
        ...state,
        targets: [...(state.targets || []), ...action.targets]
      };
    case 'TARGETING_DELETE_REQUEST':
      return {
        ...state,
        deleting: [...new Set([...state.deleting, action.target.id])]
      };
    case 'TARGETING_DELETE_RECEIVE':
      return {
        deleting: [...state.deleting.filter(id => id !== action.target.id)],
        targets: [...state.targets.filter(({ id }) => id !== action.target.id)]
      };
    case 'TARGETING_DELETE_FAILURE':
      return {
        deleting: [...state.deleting.filter(id => id !== action.target.id)]
      };
    default:
      return state;
  }
}
