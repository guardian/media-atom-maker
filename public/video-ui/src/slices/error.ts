import { createSlice } from '@reduxjs/toolkit';
import { Action, AnyAction } from 'redux';

const SHOW_ERROR = 'SHOW_ERROR' as const;
type ShowError = AnyAction & { type: typeof SHOW_ERROR; message: string };

export const showError: (message: string) => ShowError = message => {
  return {
    type: SHOW_ERROR,
    message
  };
};

export const clearError: () => Action<'CLEAR_ERROR'> = () => {
  return {
    type: 'CLEAR_ERROR'
  };
};

type ErrorState = false | string;

const initialState = false as ErrorState;

const error = createSlice({
  name: 'error',
  initialState,
  reducers: {},
  extraReducers: builder => ({
    showError: builder.addCase<'SHOW_ERROR', ShowError>(
      'SHOW_ERROR',
      (state, { message }: { message: string }) => (state = message)
    ),
    clearError: builder.addCase('CLEAR_ERROR', state => (state = false))
  })
});

export default error.reducer;
