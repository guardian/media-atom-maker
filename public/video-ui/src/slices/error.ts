import { createSlice } from '@reduxjs/toolkit';
import { Action, AnyAction } from 'redux';
import Raven from 'raven-js';
import { setActiveAsset } from './video';

const SHOW_ERROR = 'SHOW_ERROR' as const;
const SHOW_WARNING = 'SHOW_WARNING' as const;

type ShowError = AnyAction & { type: typeof SHOW_ERROR; message: string };
type ShowWarning = AnyAction & { type: typeof SHOW_WARNING; message: string };

export const showError: (message: string, error?: unknown) => ShowError = (
  message,
  error = undefined
) => {
  if (error && error instanceof Error) {
    Raven.captureException(error, { tags: { message } });
  }

  return {
    type: SHOW_ERROR,
    message
  };
};

export const showWarning: (message: string) => ShowWarning = message => {
  return {
    type: SHOW_WARNING,
    message
  };
};

export const clearErrorAndWarning: () => Action<'CLEAR_ERROR_AND_WARNING'> =
  () => ({
    type: 'CLEAR_ERROR_AND_WARNING'
  });

interface Error {
  message: false | string;
  key: number;
  warningMessage: false | string;
  warningKey: number;
}

const initialState: Error = {
  message: false,
  key: 0,
  warningMessage: false,
  warningKey: 0
};

// Currently this slice users Extra Reducers to all for support of actions without
// `domain/action` type formats. Once all consuming code which dispatches error actions
// is using the new functions we can use the standard reducer pattern.
const error = createSlice({
  name: 'error',
  initialState,
  reducers: {},
  extraReducers: builder => ({
    showError: builder.addCase<'SHOW_ERROR', ShowError>(
      'SHOW_ERROR',
      (state, { message }: ShowError) => {
        state.message = message;
        state.key++;
      }
    ),
    showWarning: builder.addCase<'SHOW_WARNING', ShowWarning>(
      'SHOW_WARNING',
      (state, { message }: ShowWarning) => {
        state.warningMessage = message;
        state.warningKey++;
      }
    ),
    clearErrorAndWarning: builder.addCase('CLEAR_ERROR_AND_WARNING', state => {
      state.message = false;
      state.warningMessage = false;
    }),
    showDurationWarning: builder.addCase(
      setActiveAsset,
      (state, { payload }) => {
        const activeAsset = payload.assets.find(
          a => a.version === payload.activeVersion
        );
        if (payload.duration === 0 && activeAsset?.platform === 'Youtube') {
          state.warningMessage =
            'YouTube reported the activated asset as being Live. If this is not the case you will need to manually set the duration.';
          state.warningKey++;
        }
      }
    )
  })
});

export default error.reducer;
