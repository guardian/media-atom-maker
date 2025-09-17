import { createSlice, Action, PayloadAction } from '@reduxjs/toolkit';

export type CheckedFormFieldsState = {
  [key: string]: Record<string, string | null | undefined>;
};

type UpdateCheckedFormFieldsAction = PayloadAction<Record<string, Record<string, string | null | undefined>>>

const initialState : CheckedFormFieldsState = {};
const checkedFormFields = createSlice({
  name: 'checkedFormFields',
  initialState,
  reducers: {
    updateCheckedFormFieldsErrors(state, action: UpdateCheckedFormFieldsAction) {
      const formName = Object.keys(action.payload)[0];
      const newFormErrors = action.payload[formName];
      const currentFormErrors =  state[formName] || {};
      state[formName] = { ...currentFormErrors, ...newFormErrors };
    }
  }
});

export default checkedFormFields.reducer;

export const { updateCheckedFormFieldsErrors } = checkedFormFields.actions;
