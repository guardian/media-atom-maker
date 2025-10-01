import {createAsyncThunk, createSlice, PayloadAction} from '@reduxjs/toolkit';
import TargetingApi from "../services/TargetingApi";
import {showError} from "./error";
import WorkflowApi, {Priority} from "../services/WorkflowApi";
import {Video} from "../services/VideosApi";

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

export const createTarget = createAsyncThunk<TargetType, {id: string, title: string, expiryDate: unknown}>(
  'targeting/createTarget',
  (target, { dispatch }) =>
    (TargetingApi.createTarget(target) as Promise<TargetType>)
      .catch(err => {
        dispatch(showError(`Failed to create Target`, err))
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
    }),
    requestDeleteTarget: (
      state: TargetingState,
      action: PayloadAction<TargetType>
    ) => ({
      ...state,
      deleting: [...new Set([...state.deleting, action.payload.id])]
    }),
    receiveDeleteTarget: (
      state: TargetingState,
      action: PayloadAction<TargetType>
    ) => ({
      deleting: [...state.deleting.filter(id => id !== action.payload.id)],
      targets: [
        ...(state.targets || []).filter(({ id }) => id !== action.payload.id)
      ]
    }),
    errorDeleteTarget: (
      state: TargetingState,
      action: PayloadAction<TargetType>
    ) => ({
      ...state,
      deleting: [...state.deleting.filter(id => id !== action.payload.id)]
    })
  },
  extraReducers: builder => {
    builder.addCase(createTarget.fulfilled, (state, {payload}) => {
      state.targets = [...(state.targets || []), payload]
    })
  }
});

export default targeting.reducer;

export const {
  requestUpdateTarget,
  requestGetTargets,
  receiveGetTarget,
  requestDeleteTarget,
  receiveDeleteTarget,
  errorDeleteTarget
} = targeting.actions;
