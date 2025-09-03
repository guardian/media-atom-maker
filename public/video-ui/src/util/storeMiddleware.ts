import { clearError } from '../slices/error';
import { updatePath } from "../slices/path";
import { Middleware } from "redux";

export const storeMiddleware: Middleware =
  ({ dispatch, getState }) =>
  next =>
  action => {
    const prevState = getState();
    next(action);
    getState();

    if (prevState.path !== location.pathname) {
      dispatch(
        updatePath(location.pathname)
      );

      dispatch({
        type: 'VIDEO_POPULATE_BLANK',
        receivedAt: Date.now()
      });

      dispatch({
        type: 'USAGE_UPDATE_BLANK',
        receivedAt: Date.now()
      });

      dispatch(clearError());

      dispatch({
        type: 'VIDEO_EDIT_STATE_REQUEST',
        state: false,
        receivedAt: Date.now()
      });
    }
  };
