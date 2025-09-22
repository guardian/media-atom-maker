import { clearError } from '../slices/error';
import { updatePath } from "../slices/path";
import { setVideoBlank } from "../slices/video";
import { Middleware } from "redux";
import { setUsageToBlank } from '../slices/usage';
import { updateVideoEditState } from "../slices/editState";


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

      dispatch(updateVideoEditState(false));
    }
  };
