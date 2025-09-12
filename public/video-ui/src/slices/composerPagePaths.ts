import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import VideosApi, { PathSyncCheckReport } from '../services/VideosApi';
import { showError } from './error';

type ComposerPathPathState = {
  pathSyncCheckReports: Record<string, PathSyncCheckReport>
}

const initialState: ComposerPathPathState = {
  pathSyncCheckReports: {}
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
      .addCase(fetchComposerPathReport.fulfilled, (state, action) => {
        state.pathSyncCheckReports[action.meta.arg] = action.payload;
        return state;
      });
  }
});

export default composerPagePaths.reducer;