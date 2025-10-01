import WorkflowApi, {WorkflowDetails} from '../../services/WorkflowApi';
import { showError } from "../../slices/error";
import { AppDispatch } from "../../util/setupStore";

export function trackInWorkflow({ video, status, section, note, prodOffice, priority }: WorkflowDetails) {
  return (dispatch: AppDispatch) => {
    return WorkflowApi.trackInWorkflow({ video, status, section, note, prodOffice, priority })
      .catch(err => dispatch(showError('Failed to track Atom in Workflow', err)));
  };
}
