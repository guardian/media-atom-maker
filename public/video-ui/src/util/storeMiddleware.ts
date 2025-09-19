import { clearError } from '../slices/error';
import { updatePath } from "../slices/path";
import { setVideoBlank } from "../slices/video";
import { Middleware } from "redux";
import { setUsageToBlank } from '../slices/usage';

export const storeMiddleware: Middleware =
  ({ dispatch, getState }) =>
  next =>
  action => {
    const prevState = getState();
    next(action);
    getState();

    if (prevState.path !== location.pathname) {
      dispatch(updatePath(location.pathname));

      dispatch(setVideoBlank());

      dispatch(setUsageToBlank());

      dispatch(clearError());

      dispatch({
        type: 'VIDEO_EDIT_STATE_REQUEST',
        state: false,
        receivedAt: Date.now()
      });
    }
  };
