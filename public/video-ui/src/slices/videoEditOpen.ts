import {createSlice, Action, PayloadAction} from '@reduxjs/toolkit';


const initialState: boolean = false;
const videoEditOpen = createSlice({
  name: 'editState',
  initialState,
  reducers: {
    updateVideoEditState(state:boolean, action: PayloadAction<boolean>) {
     return  action.payload
    }
  }
});

export default videoEditOpen.reducer;

export const { updateVideoEditState } = videoEditOpen.actions;
