import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import WorkflowApi from "../services/WorkflowApi";
import { showError } from "./error";
import { defaultWorkflowStatusData } from '../constants/defaultWorkflowStatusData';

type WorkflowState = {
  sections: unknown[];
  statuses: unknown[];
  status: Status;
  priorities: unknown[];
};

type Status = object

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

export const getStatus = createAsyncThunk<{status: Status}, {id: unknown}>(
  'workflow/getStatus',
  (video, { dispatch }) => WorkflowApi.getAtomInWorkflow(video).catch(
    (error: Response | any) => {
      if (error instanceof Response) {
        if (error.status !== 404) {
          return dispatch(errorReceivingStatus(error));
        }
        try {
          error.json().then(errorBody => {
            if (errorBody.errors && errorBody.errors.message === 'ContentNotFound') {
              return dispatch(receiveStatus404());
            }
          });
        } catch (e) {
          // failed to parse response as json
          return dispatch(errorReceivingStatus(error));
        }
      }
    }
  )
)

function errorReceivingStatus(error: unknown) {
  return showError(
    'Cannot get Atom status in Workflow',
    error,
  );
}

function receiveStatus404() {
  return {
    type: 'WORKFLOW_STATUS_NOT_FOUND',
    receivedAt: Date.now(),
    status: defaultWorkflowStatusData()
  };
}


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
    .addCase(getStatus.fulfilled, (state, action) => {
    state.status = {isTrackedInWorkflow: true, ...action.payload.status}
  })
})

export default workflow.reducer
