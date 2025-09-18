import {QUERY_PARAM_mediaPlatformFilter} from "../constants/queryParams";

const initialState = new URL(window.location.href).searchParams.get(QUERY_PARAM_mediaPlatformFilter);
export default function mediaPlatformFilter(state = initialState, action) {
  switch (action.type) {
    case 'UPDATE_MEDIA_PLATFORM_FILTER':
      return action.mediaPlatformFilter;
    default:
      return state;
  }
}
