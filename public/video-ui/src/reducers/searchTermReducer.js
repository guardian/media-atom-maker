export default function searchTerm(state = "", action) {
  switch (action.type) {
    case 'UPDATE_SEARCH_TERM':
      return action.searchTerm || "";
    default:
      return state;
  }
}
