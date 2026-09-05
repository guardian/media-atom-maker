import { createSlice } from '@reduxjs/toolkit';
import { Action, AnyAction } from 'redux';
import * as Sentry from '@sentry/browser';
import { setActiveAsset } from './video';

const SHOW_ERROR = 'SHOW_ERROR' as const;
const SHOW_WARNING = 'SHOW_WARNING' as const;

type ShowError = AnyAction & { type: typeof SHOW_ERROR; message: string };
type ShowWarning = AnyAction & { type: typeof SHOW_WARNING; message: string };

function reportToSentry(message: string, error: unknown): void {
  // A real Error carries a meaningful stack, so let Sentry group on it as-is.
  if (error instanceof Error) {
    Sentry.captureException(error, { extra: { message } });
    return;
  }

  // `apiRequest` throws the raw Response for any non-2xx, so a large share of
  // the values arriving here are Responses rather than Errors. The `typeof`
  // check is required because jsdom does not define Response, so a bare
  // `instanceof` would throw a ReferenceError under Jest.
  const isResponse =
    typeof Response !== 'undefined' && error instanceof Response;

  const synthetic = new Error(
    isResponse
      ? `${message} (HTTP ${error.status} ${error.statusText})`
      : message,
    { cause: error }
  );

  Sentry.captureException(synthetic, {
    // Every synthetic Error is constructed on the line above, so they all share
    // an identical stack trace. Sentry's default grouping keys on the stack
    // rather than the message, so without an explicit fingerprint unrelated
    // failures would collapse into a single issue.
    fingerprint: ['showError', message],
    extra: { message },
    contexts: isResponse
      ? {
          response: {
            status: error.status,
            statusText: error.statusText,
            url: error.url,
            retryAfter: error.headers.get('retry-after')
          }
        }
      : undefined
  });
}

export const showError: (message: string, error?: unknown) => ShowError = (
  message,
  error = undefined
) => {
  if (error !== undefined && error !== null) {
    reportToSentry(message, error);
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

interface ErrorState {
  message: false | string;
  key: number;
  warningMessage: false | string;
  warningKey: number;
}

const initialState: ErrorState = {
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
