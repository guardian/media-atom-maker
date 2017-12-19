export default function plutoVideos(state = [], action) {
  switch (action.type) {
    case 'PLUTO_VIDEOS_GET_RECEIVE':
      return action.videos || [];
    case 'ADD_PROJECT_RECEIVE':
      return action.video.plutoData.projectId
        ? state.filter(video => video.id !== action.video.id)
        : state.map(v => v.id === action.video.id ? action.video : v);
    default:
      return state;
  }
}
