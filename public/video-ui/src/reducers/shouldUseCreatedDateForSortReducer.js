export default function shouldUseCreatedDateForSort(state = '', action) {
  switch (action.type) {
    case 'UPDATE_SHOULD_USE_CREATED_DATE_FOR_SORT':
      return action.shouldUseCreatedDateForSort || false;
    default:
      return state;
  }
}
