import {QUERY_PARAM_shouldFilterForSelfHosted} from "../constants/queryParams";

const initialState = new URL(window.location.href).searchParams.get(QUERY_PARAM_shouldFilterForSelfHosted) === "true";
export default function shouldFilterForSelfHosted(state = initialState, action) {
  switch (action.type) {
    case 'UPDATE_SHOULD_FILTER_FOR_SELF_HOSTED':
      return action.shouldFilterForSelfHosted || false;
    default:
      return state;
  }
}
