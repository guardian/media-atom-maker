import { createSlice,Action } from '@reduxjs/toolkit';

type UpdateEditStateAction = Action  & {
  payload: boolean;
}


const initialState = false;
const videoEditOpen = createSlice({
  name: 'editState',
  initialState,
  reducers: {
    updateVideoEditState(state, action: UpdateEditStateAction) {
     return  action.payload
    }
  }
});

export default videoEditOpen.reducer;

export const { updateVideoEditState } = videoEditOpen.actions;
