import {createSlice, PayloadAction} from '@reduxjs/toolkit';


const initialState: boolean = false;
const editState = createSlice({
  name: 'editState',
  initialState,
  reducers: {
    updateVideoEditState(state:boolean, action: PayloadAction<boolean>) {
     return action.payload;
    }
  }
});

export default editState.reducer;

export const { updateVideoEditState } = editState.actions;
