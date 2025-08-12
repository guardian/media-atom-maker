import { KnownAction } from "../actions";

export function updateSearchTerm(searchTerm: string): KnownAction {
  return {
    type: 'UPDATE_SEARCH_TERM',
    searchTerm: searchTerm,
    receivedAt: Date.now()
  };
}
