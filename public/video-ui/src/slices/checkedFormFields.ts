import { createSlice, Action, PayloadAction } from '@reduxjs/toolkit';

export type CheckedFormFieldsState = {
  [key: string]: any;
};

type UpdateCheckedFormFieldsAction = PayloadAction<Record<string, any>>

const initialState : CheckedFormFieldsState = {};
const checkedFormFields = createSlice({
  name: 'checkedFormFields',
  initialState,
  reducers: {
    updateCheckedFormFieldsErrors(state, action: UpdateCheckedFormFieldsAction) {
      const formName = Object.keys(action.payload)[0];
      const newFormErrors = action.payload[formName];
      const currentFormErrors =  state[formName] || {};
      const updatedFormErrors = Object.assign({}, currentFormErrors, newFormErrors);
      const updatedForm = { [formName]: updatedFormErrors };
      return Object.assign({}, state,updatedForm);
    }
  }
});

export default checkedFormFields.reducer;

export const { updateCheckedFormFieldsErrors } = checkedFormFields.actions;
