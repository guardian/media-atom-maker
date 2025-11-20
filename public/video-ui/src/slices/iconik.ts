import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import {
  getIconikCommissionsForWorkingGroup,
  getIconikProjectsForCommission,
  getIconikWorkingGroups,
  IconikCommission,
  IconikProject,
  IconikWorkingGroup
} from '../services/IconikApi';
import { showError } from './error';

export type IconikState = {
  workingGroups: IconikWorkingGroup[];
  commissions: IconikCommission[];
  projects: IconikProject[];
};

const initialState: IconikState = {
  workingGroups: [],
  commissions: [],
  projects: []
};

export const fetchIconikWorkingGroups = createAsyncThunk<IconikWorkingGroup[]>(
  'iconik/fetchIconikWorkingGroups',
  async (_, { dispatch }) =>
    getIconikWorkingGroups().catch((error: unknown) => {
      dispatch(showError('Could not get Iconik data', error));
      throw error;
    })
);

export const fetchIconikCommissions = createAsyncThunk<
  IconikCommission[],
  string
>('iconik/fetchIconikCommissions', async (workingGroupId, { dispatch }) =>
  getIconikCommissionsForWorkingGroup({ workingGroupId }).catch(
    (error: unknown) => {
      dispatch(showError('Could not get Iconik commissions', error));
      throw error;
    }
  )
);

export const fetchIconikProjects = createAsyncThunk<IconikProject[], string>(
  'iconik/fetchIconikProjects',
  async (commissionId, { dispatch }) =>
    getIconikProjectsForCommission({ commissionId }).catch((error: unknown) => {
      dispatch(showError('Could not get Iconik projects', error));
      throw error;
    })
);

const iconik = createSlice({
  name: 'iconik',
  initialState,
  reducers: {
    resetCommissions: state => {
      state.commissions = [];
    },
    resetProjects: state => {
      state.projects = [];
    }
  },
  extraReducers(builder) {
    builder.addCase(fetchIconikWorkingGroups.fulfilled, (state, action) => {
      state.workingGroups = action.payload;
    });
    builder.addCase(fetchIconikCommissions.fulfilled, (state, action) => {
      state.commissions = action.payload;
    });
    builder.addCase(fetchIconikProjects.fulfilled, (state, action) => {
      state.projects = action.payload;
    });
  }
});

export const iconikReducer = iconik.reducer;
export const { resetCommissions, resetProjects } = iconik.actions;
