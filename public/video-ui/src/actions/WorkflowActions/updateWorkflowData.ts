import WorkflowApi, {FlatStub} from '../../services/WorkflowApi';
import { showError } from "../../slices/error";
import {AppDispatch} from "../../util/setupStore";

export function updateWorkflowData(workflowItem: FlatStub<string, string> & {id: string}) {
  return (dispatch: AppDispatch) => {
    return Promise.all([
      WorkflowApi.updateStatus(workflowItem),
      WorkflowApi.updateNote(workflowItem),
      WorkflowApi.updatePriority(workflowItem),
      WorkflowApi.updateProdOffice(workflowItem)
    ])
      .catch(err => dispatch(showError('Failed to update Atom data in Workflow', err)));
  };
}
