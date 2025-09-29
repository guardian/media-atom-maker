import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import WorkflowApi, { ExpandedStatus, FlatStub, Priority } from "../services/WorkflowApi";
import { Section } from "../services/WorkflowApi";
import { showError } from "./error";
import { defaultWorkflowStatusData } from '../constants/defaultWorkflowStatusData';


type WorkflowState = {
  sections: Section[];
  statuses: ExpandedStatus[];
  status: FlatStub<string, string> & { isTrackedInWorkflow: boolean } | {};
  priorities: Priority[];
};

export const getSections = createAsyncThunk<Section[] | undefined>(
  'workflow/getSections',
  (_, { dispatch }) => WorkflowApi.getSections().catch(
    (error: unknown) => {
      return void dispatch(
        showError(
          `Could not get Workflow sections. <a href="${WorkflowApi.workflowUrl}" target="_blank" rel="noopener">Open Workflow to get a cookie.</a>`,
          error
        )
      );
    }
  )
);

export const getStatus = createAsyncThunk<FlatStub<string, string> | undefined, {id: string}>(
  'workflow/getStatus',
  (video, { dispatch }) => WorkflowApi.getAtomInWorkflow(video).catch(
    (error: Response | any) => {
      if (error instanceof Response) {
        if (error.status !== 404) {
          return void dispatch(errorReceivingStatus(error));
        }
        try {
          error.json().then(errorBody => {
            if (errorBody.errors && errorBody.errors.message === 'ContentNotFound') {
              return void dispatch(statusNotFound(defaultWorkflowStatusData()));
            }
          });
        } catch (e) {
          // failed to parse response as json
          return void dispatch(errorReceivingStatus(error));
        }
      }
    }
  )
);

export const getStatuses = createAsyncThunk<ExpandedStatus[] | undefined>(
  'workflow/getStatuses',
  (_, { dispatch }) => WorkflowApi.getStatuses().catch(err => void dispatch(errorReceivingStatuses(err)))
);

export const getPriorities = createAsyncThunk<Priority[] | undefined>(
  'workflow/getPriorities',
  (_, { dispatch }) => WorkflowApi.getPriorities().catch(err => void dispatch(errorReceivingPriorities(err)))
);

function errorReceivingPriorities(error: unknown) {
  return showError(
    `Could not get Workflow priorities. <a href="${WorkflowApi.workflowUrl}" target="_blank" rel="noopener">Open Workflow to get a cookie.</a>`,
    error
  );
}


function errorReceivingStatus(error: unknown) {
  return showError(
    'Cannot get Atom status in Workflow',
    error
  );
}

function errorReceivingStatuses(error: unknown) {
  return showError(
    `Could not get Workflow statuses. <a href="${WorkflowApi.workflowUrl}" target="_blank" rel="noopener">Open Workflow to get a cookie.</a>`,
    error
  );
}

const initialState: WorkflowState = {
  sections: [],
  statuses: [],
  status: {},
  priorities: []
};

const workflow = createSlice({
  name: 'workflow',
  initialState,
  reducers: {
    localUpdateWorkflowData(state, action) {
      state.status = action.payload;
    },
    statusNotFound(state, action) {
      state.status = {
        isTrackedInWorkflow: false,
        ...action.payload
      };
    }
  },
  extraReducers: builder => builder
    .addCase(getSections.fulfilled, (state, action) => {
      if (action.payload) {
        state.sections = action.payload;
      }
    })
    .addCase(getStatus.fulfilled, (state, action) => {
      state.status = { isTrackedInWorkflow: true, ...action.payload };
    })
    .addCase(getStatuses.fulfilled, (state, action) => {
      if (action.payload) {
        state.statuses = action.payload;
      }
    })
    .addCase(getPriorities.fulfilled, (state, action) => {
      if (action.payload) {
        state.priorities = action.payload;
      }
    })
});

export const { localUpdateWorkflowData, statusNotFound } = workflow.actions;

export default workflow.reducer;
