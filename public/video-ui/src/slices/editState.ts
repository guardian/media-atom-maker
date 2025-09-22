import {createSlice, PayloadAction} from '@reduxjs/toolkit';


const initialState: boolean = false;
const editState = createSlice({
  name: 'editState',
  initialState,
  reducers: {
    updateVideoEditState(_, action: PayloadAction<boolean>) {
     return action.payload;
    }
  }
});

export default editState.reducer;

export const { updateVideoEditState } = editState.actions;
