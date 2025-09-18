import { createSlice,  PayloadAction } from '@reduxjs/toolkit';
import FieldNotification from "../constants/FieldNotification";

export type CheckedFormFieldsState = Record<string, Record<string, FieldNotification>>;

type UpdateCheckedFormFieldsAction = PayloadAction<CheckedFormFieldsState>

const initialState : CheckedFormFieldsState = {};
const checkedFormFields = createSlice({
  name: 'checkedFormFields',
  initialState,
  reducers: {
    updateFormErrors(state :CheckedFormFieldsState, action: UpdateCheckedFormFieldsAction) {
      const formName = Object.keys(action.payload)[0];
      const newFormErrors = action.payload[formName];
      const currentFormErrors =  state[formName] || {};
      state[formName] = { ...currentFormErrors, ...newFormErrors };
    }
  }
});

export default checkedFormFields.reducer;

export const { updateFormErrors } = checkedFormFields.actions;
