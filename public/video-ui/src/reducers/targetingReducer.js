// null here means targets haven't been fetched, an empty array means they have
// been fetched but there are none
export default function targeting(state = { targets: null }, action) {
  switch (action.type) {
    // if we receive any targets, for now, completely overwrite what's in here
    case 'TARGETING_POST_RECEIVE':
    case 'TARGETING_GET_RECEIVE':
      return { targets: action.targets || [] };
    default:
      return state;
  }
}
