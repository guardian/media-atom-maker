import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import ErrorMessages from '../constants/ErrorMessages';
import { CapiContent, Stage as CapiStage } from '../services/capi';
import VideosApi from '../services/VideosApi';
import { showError } from './error';


export type UsageData = Record<CapiStage, {
    video: CapiContent[];
    other: CapiContent[];
}>;

export type UsageState = {
    data: UsageData;
    totalUsages: number,
    totalVideoPages: number,
    isFetching: boolean,
}

const getInitialState = (): UsageState => ({
    data: {
        published: {
            video: [],
            other: []
        },
        preview: {
            video: [],
            other: []
        }
    },

    totalUsages: 0,
    totalVideoPages: 0,
    isFetching: false
});

export const fetchUsages = createAsyncThunk<
    UsageData,
    string
>(
    'usage/fetchUsages',
    (id, { dispatch }) => {
        return VideosApi.getVideoUsages(id)
            .catch(
                (error: unknown) => {
                    dispatch(showError(ErrorMessages.usages, error));
                    throw error;
                }
            );
    }
);

const usage = createSlice({
    name: 'usage',
    initialState: getInitialState(),
    reducers: {
        setUsageToBlank(state) {
            const initialState = getInitialState();
            state.data = initialState.data;
            state.totalUsages = initialState.totalUsages;
            state.totalVideoPages = initialState.totalVideoPages;
        },
        updateVideoUsageWebTitle(state, action: PayloadAction<string>) {
            const updateWebtitle = (usage: CapiContent): CapiContent => ({ ...usage, webTitle: action.payload });
            state.data.preview.video = state.data.preview.video.map(updateWebtitle);
            state.data.published.video = state.data.published.video.map(updateWebtitle);
        },
        addNewlyCreatedVideoUsage(state, action: PayloadAction<CapiContent>) {
            state.data.preview.video = [action.payload, ...state.data.preview.video];
            state.totalUsages = state.totalUsages + 1;
            state.totalVideoPages = state.totalVideoPages + 1;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchUsages.pending, (state) => {
                state.isFetching = true;
            })
            .addCase(fetchUsages.fulfilled, (state, {payload}) => {
                state.isFetching = false;
                state.data = payload;

                const { preview, published } = payload;
                const totalVideoPages = preview.video.length + published.video.length;
                const totalUsages = totalVideoPages + preview.other.length + published.other.length;

                state.totalUsages = totalUsages;
                state.totalVideoPages = totalVideoPages;
            });
    }
});


export default usage.reducer;

export const { setUsageToBlank, updateVideoUsageWebTitle, addNewlyCreatedVideoUsage } = usage.actions;