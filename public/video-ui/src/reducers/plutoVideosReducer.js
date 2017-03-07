export default function plutoVideos(state = [], action) {
  switch (action.type) {

    case 'PLUTO_VIDEOS_GET_RECEIVE':
      return action.videos || [];
    case 'ADD_PROJECT_GET_RECEIVE':
      return [];
    default:
      return state;
  }
}
