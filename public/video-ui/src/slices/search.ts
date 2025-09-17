import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import {QUERY_PARAM_shouldUseCreatedDateForSort} from "../constants/queryParams";


export interface Search {
  searchTerm: string;
  shouldUseCreatedDateForSort: boolean;
}

const initialState: Search = {
  searchTerm: '',
  shouldUseCreatedDateForSort: new  URLSearchParams(window.location.search).get(QUERY_PARAM_shouldUseCreatedDateForSort) === "true"
};

const search = createSlice({
  name: 'search',
  initialState,
  reducers: {
    updateSearchTerm: (state, { payload }: PayloadAction<string>) =>
    { state.searchTerm = payload },
    updateShouldUseCreatedDateForSort: (state, { payload }: PayloadAction<boolean>) =>
    { state.shouldUseCreatedDateForSort = payload }
  },
});

export default search.reducer;

export const { updateSearchTerm, updateShouldUseCreatedDateForSort } =
  search.actions;
