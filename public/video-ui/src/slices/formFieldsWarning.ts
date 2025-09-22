import {createSlice, PayloadAction} from '@reduxjs/toolkit';

const initialState : Record<string, boolean> = {};
const formFieldsWarning = createSlice({
  name: 'formFieldsWarning',
  initialState,
  reducers: {
    updateFormWarnings(state:Record<string, boolean>, action: PayloadAction<boolean>) {
      Object.assign(state, action.payload);
    }
  }
});

export default formFieldsWarning.reducer;

export const { updateFormWarnings } = formFieldsWarning.actions;
