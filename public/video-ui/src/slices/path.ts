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
            state = action.payload;
            return state;
        }
    }
});

export default path.reducer;

export const { updatePath } = path.actions;
