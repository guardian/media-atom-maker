export default function uploads(state = [], action) {
  switch(action.type) {
    case 'RUNNING_UPLOADS':
      return action.uploads;

    default:
      return state;
  }
}