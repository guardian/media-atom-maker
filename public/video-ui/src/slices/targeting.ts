import {createSlice, PayloadAction} from '@reduxjs/toolkit';

type TargetType={
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
type TargetingState = {
  targets: TargetType[]| null,
  deleting: string[],
};

const initialState:TargetingState = { targets: null, deleting: [] };
const targeting = createSlice({
  name: 'targeting',
  initialState,
  reducers: {
    requestUpdateTarget:(state:TargetingState, action :PayloadAction<TargetType>)=> ({
        ...state,
        targets: [
          ...(state.targets || []).filter(({ id }) => id !== action.payload.id),
          action.payload
        ]
    }),
    receiveUpdateTarget:(state:TargetingState)=> ({
      ...state
    }),
    // when we start a new request for targets reset to our 'loading' state
    // this gets called when we look for targets for a new video
    requestGetTargets:()=> ({
       ...initialState
    }),
    requestCreateTarget:(state:TargetingState)=> ({
      ...state
    }),
    // if we receive any targets, for now, completely overwrite what's in here
    receiveCreateTarget:(state:TargetingState, action :PayloadAction<TargetType>)=> ({
    ...state,
        targets: [...(state.targets || []), action.payload]
    }),
    receiveGetTarget:(state:TargetingState, action: PayloadAction<TargetType[]>)=> ({
      ...state,
      targets: [...(state.targets || []), ...action.payload]
    }),
    requestDeleteTarget:(state:TargetingState, action :PayloadAction<TargetType>)=> ({
      ...state,
      deleting: [...new Set([...state.deleting, action.payload.id])]
    }),
    receiveDeleteTarget:(state:TargetingState, action :PayloadAction<TargetType>)=> ({
      deleting: [...state.deleting.filter(id => id !== action.payload.id)],
      targets: [...(state.targets || []).filter(({ id }) => id !== action.payload.id)]
    }),
    errorDeleteTarget:(state:TargetingState, action:PayloadAction<TargetType>)=> ({
      ...state,
      deleting: [...state.deleting.filter(id => id !== action.payload.id)]
    })
  }
});

export default targeting.reducer;

export const {  requestUpdateTarget,receiveUpdateTarget,requestGetTargets,requestCreateTarget, receiveCreateTarget,receiveGetTarget, requestDeleteTarget, receiveDeleteTarget,errorDeleteTarget} = targeting.actions;
