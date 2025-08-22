import { createSlice, Action } from '@reduxjs/toolkit';


export type PathState = string;

type UpdatePathAction = Action & {
    payload: string
}
const initialState: PathState = '';

const path = createSlice({
    name: 'path',
    initialState,
    reducers: {
        updatePath(state, action: UpdatePathAction) {
            if (state === action.payload) {
                return state;
            }
            return action.payload;
        }
    }
});

export default path.reducer;

export const { updatePath } = path.actions;
