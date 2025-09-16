import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import WorkflowApi from "../services/WorkflowApi";
import { showError } from "./error";

type WorkflowState = {
  sections: unknown[];
  statuses: unknown[];
  status: unknown;
  priorities: unknown[];
};

export const getSections = createAsyncThunk(
  'workflow/getSections',
  ({}, { dispatch }) => WorkflowApi.getSections().catch(
    (error: unknown) => {
      dispatch(
        showError(
          `Could not get Workflow sections. <a href="${WorkflowApi.workflowUrl}" target="_blank" rel="noopener">Open Workflow to get a cookie.</a>`,
          error
        )
      )
    }
  )
)

const initialState: WorkflowState = {
  sections: [],
  statuses: [],
  status: {},
  priorities: [],
}

const workflow = createSlice({
  name: 'workflow',
  initialState,
  reducers: {},
  extraReducers: builder => builder
    .addCase(getSections.fulfilled, (state, action) => {
      state.sections = action.payload.sections || []
    })
})

export default workflow.reducer
