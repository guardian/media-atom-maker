export default function videos(state = [], action) {
  switch (action.type) {

    case 'VIDEOS_GET_RECEIVE':
      return action.videos || [];

    default:
      return state;
  }
}
