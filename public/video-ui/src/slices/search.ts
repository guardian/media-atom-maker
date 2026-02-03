import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import {
  QUERY_PARAM_videoPlayerFormatFilter,
  QUERY_PARAM_shouldUseCreatedDateForSort
} from '../constants/queryParams';

export interface Search {
  searchTerm: string;
  shouldUseCreatedDateForSort: boolean;
  videoPlayerFormatFilter: string;
}

const searchParams = new URLSearchParams(window.location.search);

const initialState: Search = {
  searchTerm: '',
  shouldUseCreatedDateForSort:
    searchParams.get(QUERY_PARAM_shouldUseCreatedDateForSort) === 'true',
  videoPlayerFormatFilter: searchParams.get(QUERY_PARAM_videoPlayerFormatFilter)
};

const search = createSlice({
  name: 'search',
  initialState,
  reducers: {
    updateSearchTerm: (state, { payload }: PayloadAction<string>) => {
      state.searchTerm = payload;
    },
    updateShouldUseCreatedDateForSort: (
      state,
      { payload }: PayloadAction<boolean>
    ) => {
      state.shouldUseCreatedDateForSort = payload;
    },
    updateVideoPlayerFormatFilter: (state, { payload }: PayloadAction<string>) => {
      state.videoPlayerFormatFilter = payload;
    }
  }
});

export default search.reducer;

export const {
  updateSearchTerm,
  updateShouldUseCreatedDateForSort,
  updateVideoPlayerFormatFilter
} = search.actions;
