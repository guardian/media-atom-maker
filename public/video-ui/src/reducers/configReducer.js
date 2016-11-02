export default function config(state = false, action) {
  switch (action.type) {

    case 'CONFIG_RECEIVED':
        return action.config || false;


    default:
      return state;
  }
}
