export default function atoms(state = [], action) {
  switch (action.type) {

    case 'ATOMS_GET_RECIEVE':
      return action.atoms || [];

    default:
      return state;
  }
}
