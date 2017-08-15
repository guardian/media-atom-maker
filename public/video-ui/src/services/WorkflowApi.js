import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';

export default class WorkflowApi {
  static get workflowUrl() {
    return getStore().getState().config.workflowUrl;
  }

  static getSections() {
    return pandaReqwest({
      url: `${WorkflowApi.workflowUrl}/api/sections`,
      crossOrigin: true,
      withCredentials: true
    }).then(_ => _.data);
  }
}
