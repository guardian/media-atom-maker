import { KnownAction } from "../../actions";

export function updateShouldUseCreatedDateForSort(shouldUseCreatedDateForSort: boolean): KnownAction {
  return {
    type: 'UPDATE_SHOULD_USE_CREATED_DATE_FOR_SORT',
    shouldUseCreatedDateForSort,
    receivedAt: Date.now()
  };
}
