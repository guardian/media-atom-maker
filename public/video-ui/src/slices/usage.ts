import { Action, createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import ErrorMessages from '../constants/ErrorMessages';
import { CapiContent, Stage as CapiStage } from '../services/capi';
import VideosApi from '../services/VideosApi';
import { showError } from './error';
import { AnyAction } from 'redux';


const VIDEO_PAGE_CREATE_POST_RECEIVE = 'VIDEO_PAGE_CREATE_POST_RECEIVE' as const;
type PageCreationRecieveAction = AnyAction & { type: typeof VIDEO_PAGE_CREATE_POST_RECEIVE; newPage: CapiContent };


export type UsageData = {
    data: Record<CapiStage, {
        video: CapiContent[];
        other: CapiContent[];
    }>;

    totalUsages: number,
    totalVideoPages: number,
}

const getInitialState = (): UsageData => ({
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
    totalVideoPages: 0
});

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
            const initialState = getInitialState();
            state.data = initialState.data;
            state.totalUsages = initialState.totalUsages;
            state.totalVideoPages = initialState.totalVideoPages;
        },
        updateVideoUsageWebTitle(state, action: Action & { payload: string }) {
            const updateWebtitle = (usage: CapiContent): CapiContent => ({ ...usage, webTitle: action.payload });
            state.data.preview.video = state.data.preview.video.map(updateWebtitle);
            state.data.published.video = state.data.published.video.map(updateWebtitle);
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchUsages.fulfilled, (state, action) => {
                state.data = action.payload.data;
                state.totalUsages = action.payload.totalUsages;
                state.totalVideoPages = action.payload.totalVideoPages;
            })
            .addCase<'VIDEO_PAGE_CREATE_POST_RECEIVE', PageCreationRecieveAction>('VIDEO_PAGE_CREATE_POST_RECEIVE', (state, action) => {
                state.data.preview.video = [action.newPage, ...state.data.preview.video];
                state.totalUsages = state.totalUsages + 1;
                state.totalVideoPages = state.totalVideoPages + 1;
            });
    }
});


export default usage.reducer;

export const { setUsageToBlank, updateVideoUsageWebTitle } = usage.actions;