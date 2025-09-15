import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { blankUsageData } from '../constants/blankUsageData';
import { CapiContent, Stage as CapiStage } from '../services/capi';
import VideosApi, { Video } from '../services/VideosApi';
import { showError } from './error';
import ErrorMessages from '../constants/ErrorMessages';

export type UsageData = {
    data: Record<CapiStage, {
        video: CapiContent[];
        other: CapiContent[];
    }>;

    totalUsages: number,
    totalVideoPages: number,
}

const getInitialState = () => structuredClone(blankUsageData);

export const fetchUsages = createAsyncThunk<
    UsageData,
    string
>(
    'usage/fetchUsages',
    (id, { dispatch }) =>
        VideosApi.getVideoUsages(id)
            .catch(
                (error: unknown) => {
                    dispatch(showError(ErrorMessages.usages, error));
                    throw error;
                }
            )
);

const usage = createSlice({
    name: 'usage',
    initialState: getInitialState(),
    reducers: {
        setUsageToBlank(state) {
            const initialState =  getInitialState();
            state.data = initialState.data;
            state.totalUsages = initialState.totalUsages;
            state.totalVideoPages = initialState.totalVideoPages;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchUsages.pending, (_state, action) => {
                console.log('pending', action);
            })
            .addCase(fetchUsages.fulfilled, (state, action) => {
                console.log('fulfilled', action);
                state.data = action.payload.data;
                state.totalUsages = action.payload.totalUsages;
                state.totalVideoPages = action.payload.totalVideoPages;
            });
    }
});


export default usage.reducer;

export const { setUsageToBlank } = usage.actions;