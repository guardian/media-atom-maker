import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import VideosApi, { PathSyncCheckReport } from '../services/VideosApi';
import { showError } from './error';

export type ComposerPathPathState = {
  pathSyncCheckReports: Record<string, PathSyncCheckReport>
  pendingChecks: string[]
}

const initialState: ComposerPathPathState = {
  pathSyncCheckReports: {},
  pendingChecks: []
};


export const fetchComposerPathReport = createAsyncThunk<
  PathSyncCheckReport, string
>(
  'composerPagePaths/fetchComposerPathReport',
  (composerId, { dispatch }) => {
    return VideosApi.fetchComposerPathReport(composerId)
      .catch(error => {
        dispatch(showError('failed to fetch composer path report', error));
        throw error;
      });
  }
);

const composerPagePaths = createSlice({
  name: 'composerPagePaths',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder
      .addCase(fetchComposerPathReport.pending, (state, action) => {
        state.pendingChecks.push(action.meta.arg);
        return state;
      })
      .addCase(fetchComposerPathReport.fulfilled, (state, action) => {
        state.pendingChecks = state.pendingChecks.filter(id => id !== action.meta.arg);
        state.pathSyncCheckReports[action.meta.arg] = action.payload;
        return state;
      });
  }
});

export default composerPagePaths.reducer;