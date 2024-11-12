export const storeMiddleware = ({ dispatch, getState }) => next => action => {
  const prevState = getState();
  next(action);
  getState();

  if (prevState.path !== location.pathname) {
    dispatch({
      type: 'PATH_UPDATE',
      path: location.pathname,
      receivedAt: Date.now()
    });

    dispatch({
      type: 'VIDEO_POPULATE_BLANK',
      receivedAt: Date.now()
    });

    dispatch({
      type: 'USAGE_UPDATE_BLANK',
      receivedAt: Date.now()
    });

    dispatch({
      type: 'CLEAR_ERROR',
      receivedAt: Date.now()
    });

    dispatch({
      type: 'VIDEO_EDIT_STATE_REQUEST',
      state: false,
      receivedAt: Date.now()
    });
  }
};
