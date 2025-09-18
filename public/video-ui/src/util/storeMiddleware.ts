import { clearError } from '../slices/error';
import { updatePath } from "../slices/path";
import { setVideoBlank } from "../slices/video";
import { Middleware } from "redux";
import { updateVideoEditState } from "../slices/videoEditOpen";


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

      dispatch({
        type: 'USAGE_UPDATE_BLANK',
        receivedAt: Date.now()
      });

      dispatch(clearError());

      dispatch(updateVideoEditState(false));
    }
  };
