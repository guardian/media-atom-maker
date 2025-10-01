import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import TargetingApi from '../services/TargetingApi';
import { showError } from './error';

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
  targets: TargetType[] | null;
  deleting: string[];
};

export const createTarget = createAsyncThunk<
  TargetType,
  { id: string; title: string; expiryDate: unknown }
>('targeting/createTarget', (target, { dispatch }) =>
  (TargetingApi.createTarget(target) as Promise<TargetType>).catch(err => {
    dispatch(showError(`Failed to create Target`, err));
    throw err;
  })
);

export const deleteTarget = createAsyncThunk<unknown, { id: string }>(
  'targeting/deleteTarget',
  (target, { dispatch }) =>
    TargetingApi.deleteTarget(target).catch(err => {
      dispatch(showError(`Failed to delete Target`, err));
      throw err;
    })
);

const initialState: TargetingState = { targets: null, deleting: [] };

const targeting = createSlice({
  name: 'targeting',
  initialState,
  reducers: {
    requestUpdateTarget: (
      state: TargetingState,
      action: PayloadAction<TargetType>
    ) => ({
      ...state,
      targets: [
        ...(state.targets || []).filter(({ id }) => id !== action.payload.id),
        action.payload
      ]
    }),
    // when we start a new request for targets reset to our 'loading' state
    // this gets called when we look for targets for a new video
    requestGetTargets: () => ({
      ...initialState
    }),
    receiveGetTarget: (
      state: TargetingState,
      action: PayloadAction<TargetType[]>
    ) => ({
      ...state,
      targets: [...(state.targets || []), ...action.payload]
    })
  },
  extraReducers: builder => {
    builder
      .addCase(createTarget.fulfilled, (state, { payload }) => {
        state.targets = [...(state.targets || []), payload];
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

export const { requestUpdateTarget, requestGetTargets, receiveGetTarget } =
  targeting.actions;
