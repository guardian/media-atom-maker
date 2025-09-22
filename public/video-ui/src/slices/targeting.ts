import {createSlice, PayloadAction} from '@reduxjs/toolkit';
import FieldNotification from "../constants/FieldNotification";


type Targets={
  id: string,
  title: string,
  tagPaths: string[],
  url: string,
  activeUntil?: number,
  createdBy?: string,
  updatedBy?: string,
  createdAt?: string,
  updatedAt?: string,
}
export type TargetingState = {
  targets: Targets[]| null,
  deleting: string[],
};

// PayloadAction<TargetingState>


const initialState:TargetingState = { targets: null, deleting: [] };
const targeting = createSlice({
  name: 'targeting',
  initialState,
  reducers: {
    requestUpdateTarget:(state, action :PayloadAction<any>)=> ({
        ...state,
        targets: [
          ...state.targets.filter(({ id }) => id !== action.payload.id),
          action.payload
        ]
    }),
    // when we start a new request for targets reset to our 'loading' state
    // this gets called when we look for targets for a new video
    requestGetTargets:()=> ({
       ...initialState
    }),
    // if we receive any targets, for now, completely overwrite what's in here
    receiveCreateTarget:(state, action)=> ({
    ...state,
        targets: [...(state.targets || []), ...action.payload]
    }),
    receiveGetTarget:(state, action)=> ({
      ...state,
      targets: [...(state.targets || []), ...action.payload]
    }),
    requestDeleteTarget:(state, action)=> ({
      ...state,
      deleting: [...new Set([...state.deleting, action.payload.id])]
    }),
    receiveDeleteTarget:(state, action)=> ({
      deleting: [...state.deleting.filter(id => id !== action.payload.id)],
      targets: [...state.targets.filter(({ id }) => id !== action.payload.id)]
    }),
    errorDeleteTarget:(state, action)=> ({
      ...state,
      deleting: [...state.deleting.filter(id => id !== action.payload.id)]
    })
  }
});

export default targeting.reducer;

export const {  requestUpdateTarget,requestGetTargets, receiveCreateTarget,receiveGetTarget, requestDeleteTarget, receiveDeleteTarget,errorDeleteTarget} = targeting.actions;
