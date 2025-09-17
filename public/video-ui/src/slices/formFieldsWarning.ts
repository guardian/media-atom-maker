import { createSlice,Action } from '@reduxjs/toolkit';

export type FormFieldsWarningState = {
  [key: string]: any;
};

type UpdateFormFieldsWarningAction = Action  & {
  payload: string
}


const initialState : FormFieldsWarningState = {};
const formFieldsWarning = createSlice({
  name: 'formFieldsWarning',
  initialState,
  reducers: {
    updateFormWarnings(state, action: UpdateFormFieldsWarningAction) {
      Object.assign(state, action.payload);
    }
  }
});

export default formFieldsWarning.reducer;

export const { updateFormWarnings } = formFieldsWarning.actions;
