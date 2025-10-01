import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import TargetingApi from '../services/TargetingApi';
import { showError } from './error';
import debounce from 'lodash/debounce';

type TargetType = {
  id: string;
  title: string;
  tagPaths: string[];
  url: string;
  activeUntil?: number;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
};
type TargetingState = {
  targets: TargetType[];
  deleting: string[];
};

export const getTargets = createAsyncThunk<Array<TargetType>, { id: string }>(
  'targeting/getTargets',
  (video, { dispatch }) =>
    (TargetingApi.getTargets(video) as Promise<Array<TargetType>>).catch(
      err => {
        dispatch(showError('Failed to get Targets', err));
        throw err;
      }
    )
);

export const createTarget = createAsyncThunk<
  TargetType,
  { id: string; title: string; expiryDate: unknown }
>('targeting/createTarget', (target, { dispatch }) =>
  (TargetingApi.createTarget(target) as Promise<TargetType>).catch(err => {
    dispatch(showError('Failed to create Target', err));
    throw err;
  })
);

const debouncedUpdate = debounce(
  (dispatch, target) =>
    TargetingApi.updateTarget(target).catch(err => {
      dispatch(showError(`Failed to update Target`, err));
      throw err;
    }),
  500
);

export const updateTarget = createAsyncThunk<unknown, TargetType>(
  'targeting/updateTarget',
  (target, { dispatch }) => debouncedUpdate(dispatch, target)
);

export const deleteTarget = createAsyncThunk<unknown, { id: string }>(
  'targeting/deleteTarget',
  (target, { dispatch }) =>
    TargetingApi.deleteTarget(target).catch(err => {
      dispatch(showError('Failed to delete Target', err));
      throw err;
    })
);

const initialState: TargetingState = { targets: [], deleting: [] };

const targeting = createSlice({
  name: 'targeting',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder
      .addCase(getTargets.pending, () => initialState)
      .addCase(getTargets.fulfilled, (state, { payload }) => {
        state.targets = [...(state.targets || []), ...payload];
      })
      .addCase(createTarget.fulfilled, (state, { payload }) => {
        state.targets = [...(state.targets || []), payload];
      })
      .addCase(updateTarget.pending, (state, { meta }) => {
        state.targets = [
          ...(state.targets || []).filter(({ id }) => id !== meta.arg.id),
          meta.arg
        ];
      })
      .addCase(deleteTarget.pending, (state, action) => {
        state.deleting = [...new Set([...state.deleting, action.meta.arg.id])];
      })
      .addCase(deleteTarget.fulfilled, (state, action) => {
        state.deleting = [
          ...state.deleting.filter(id => id !== action.meta.arg.id)
        ];
        state.targets = [
          ...(state.targets || []).filter(({ id }) => id !== action.meta.arg.id)
        ];
      })
      .addCase(deleteTarget.rejected, (state, action) => {
        state.deleting = [
          ...state.deleting.filter(id => id !== action.meta.arg.id)
        ];
      });
  }
});

export default targeting.reducer;
