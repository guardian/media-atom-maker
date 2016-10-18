export default function videos(state = [], action) {
  switch (action.type) {

    case 'VIDEOS_GET_RECIEVE':
      return action.videos || [];

    default:
      return state;
  }
}
