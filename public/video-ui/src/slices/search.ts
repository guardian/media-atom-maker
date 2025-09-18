import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import {
  QUERY_PARAM_mediaPlatformFilter,
  QUERY_PARAM_shouldUseCreatedDateForSort
} from '../constants/queryParams';

export interface Search {
  searchTerm: string;
  shouldUseCreatedDateForSort: boolean;
  mediaPlatformFilter: string;
}

const searchParams = new URLSearchParams(window.location.search);

const initialState: Search = {
  searchTerm: '',
  shouldUseCreatedDateForSort:
    searchParams.get(QUERY_PARAM_shouldUseCreatedDateForSort) === 'true',
  mediaPlatformFilter: searchParams.get(QUERY_PARAM_mediaPlatformFilter)
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
    updateMediaPlatformFilter: (state, { payload }: PayloadAction<string>) => {
      state.mediaPlatformFilter = payload;
    }
  }
});

export default search.reducer;

export const {
  updateSearchTerm,
  updateShouldUseCreatedDateForSort,
  updateMediaPlatformFilter
} = search.actions;
