export function updateShouldUseCreatedDateForSort(shouldUseCreatedDateForSort) {
  return {
    type: 'UPDATE_SHOULD_USE_CREATED_DATE_FOR_SORT',
    shouldUseCreatedDateForSort,
    receivedAt: Date.now()
  };
}
