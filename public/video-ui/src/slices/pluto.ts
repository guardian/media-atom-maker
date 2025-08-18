import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { getPlutoCommissions, getPlutoProjects, PlutoCommission, PlutoProject } from "../services/PlutoApi";
import { KnownAction } from "../actions/actions";


const initialState: {
    commissions: PlutoCommission[];
    projects: PlutoProject[];
} = { commissions: [], projects: [] };


function errorReceivingCommissions(error: unknown): KnownAction {
    return {
        type: 'SHOW_ERROR',
        message: 'Could not get Pluto Commissions',
        receivedAt: Date.now(),
        error: error
    };
}

function errorReceivingProjects(error: unknown): KnownAction {
    return {
        type: 'SHOW_ERROR',
        message: 'Could not get Pluto Projects',
        receivedAt: Date.now(),
        error: error
    };
}


export const fetchCommissions = createAsyncThunk('pluto/fetchCommissions', (_, { dispatch }) =>
    getPlutoCommissions().catch(error => {
        dispatch(errorReceivingCommissions(error));
        throw error;
    })
);

export const fetchProjects = createAsyncThunk<PlutoProject[], string>('pluto/fetchProjects', (commissionId, { dispatch }) =>
    getPlutoProjects({ commissionId }).catch(error => {
        dispatch(errorReceivingProjects(error));
        throw error;
    })
);

const pluto = createSlice({
    name: 'pluto',
    initialState,
    reducers: {},
    extraReducers(builder) {
        builder
            .addCase(fetchCommissions.fulfilled, (state, action) => { state.commissions = action.payload; })
            .addCase(fetchProjects.fulfilled, (state, action) => { state.projects = action.payload; });
    }
});

export default pluto.reducer;
