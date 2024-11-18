export function updateSearchTerm(searchTerm) {
  return {
    type: 'UPDATE_SEARCH_TERM',
    searchTerm: searchTerm,
    receivedAt: Date.now()
  };
}
