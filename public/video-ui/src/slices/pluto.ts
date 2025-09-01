import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import {
  getPlutoCommissions,
  getPlutoProjects,
  PlutoCommission,
  PlutoProject
} from '../services/PlutoApi';
import { showError } from './error';

export type PlutoState = {
  commissions: PlutoCommission[];
  projects: PlutoProject[];
};

const initialState: PlutoState = { commissions: [], projects: [] };

export const fetchCommissions = createAsyncThunk(
  'pluto/fetchCommissions',
  (_, { dispatch }) =>
    getPlutoCommissions().catch(error => {
      dispatch(showError('Could not get Pluto Commissions', error));
      throw error;
    })
);

export const fetchProjects = createAsyncThunk<PlutoProject[], string>(
  'pluto/fetchProjects',
  (commissionId, { dispatch }) =>
    getPlutoProjects({ commissionId }).catch(error => {
      dispatch(showError('Could not get Pluto Projects', error));
      throw error;
    })
);

const pluto = createSlice({
  name: 'pluto',
  initialState,
  reducers: {},
  extraReducers(builder) {
    builder
      .addCase(fetchCommissions.fulfilled, (state, action) => {
        state.commissions = action.payload;
      })
      .addCase(fetchProjects.fulfilled, (state, action) => {
        state.projects = action.payload;
      });
  }
});

export default pluto.reducer;
