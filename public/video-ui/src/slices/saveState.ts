import { createSlice, PayloadAction } from '@reduxjs/toolkit';

const initialState: {
  saving: boolean;
  publishing: boolean;
  fetchingUsages: boolean;
  addingAsset: boolean;
  activatingAssetNumber?: number;
} = {
  saving: false,
  publishing: false,
  fetchingUsages: false,
  addingAsset: false
};

const saveState = createSlice({
  name: 'saveState',
  initialState,
  reducers: {
    setSaving: (state, { payload }: PayloadAction<boolean>) => {
      state.saving = payload;
    },
    setPublishing: (state, { payload }: PayloadAction<boolean>) => {
      state.publishing = payload;
    },
    setFetchingUsage: (state, { payload }: PayloadAction<boolean>) => {
      state.fetchingUsages = payload;
    },
    setAddingAsset: (state, { payload }: PayloadAction<boolean>) => {
      state.addingAsset = payload;
    },
    setActivatingAssetNumber: (
      state,
      { payload }: PayloadAction<number | undefined>
    ) => {
      state.saving = payload !== undefined;
      state.activatingAssetNumber = payload;
    }
  },
  extraReducers: builder => {
    builder.addCase('SHOW_ERROR', state => {
      state.saving = false;
      state.publishing = false;
      state.fetchingUsages = false;
      state.addingAsset = false;
      state.activatingAssetNumber = undefined;
    });
  }
});

export default saveState.reducer;

export const {
  setSaving,
  setPublishing,
  setFetchingUsage,
  setAddingAsset,
  setActivatingAssetNumber
} = saveState.actions;
