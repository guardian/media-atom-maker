import {QUERY_PARAM_shouldUseCreatedDateForSort} from "../constants/queryParams";

const initialState = new URL(window.location.href).searchParams.get(QUERY_PARAM_shouldUseCreatedDateForSort) === "true";
export default function shouldUseCreatedDateForSort(state = initialState, action) {
  switch (action.type) {
    case 'UPDATE_SHOULD_USE_CREATED_DATE_FOR_SORT':
      return action.shouldUseCreatedDateForSort || false;
    default:
      return state;
  }
}
