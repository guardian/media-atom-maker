import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import getProductionOffice from '../util/getProductionOffice';
import VideoUtils from '../util/video';

export default class WorkflowApi {
  static get workflowUrl() {
    return getStore().getState().config.workflowUrl;
  }

  static workflowItemLink(video) {
    return `${WorkflowApi.workflowUrl}/dashboard?editorId=${video.id}`;
  }

  static getSections() {
    // timeout in case the user is not logged into Workflow
    const params = {
      url: `${WorkflowApi.workflowUrl}/api/sections`,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params, 500).then(response => {
      return response.data
        .map(section => Object.assign({}, section, { title: section.name }))
        .sort((a, b) => {
          if (a.title.toLowerCase() < b.title.toLowerCase()) return -1;
          if (a.title.toLowerCase() > b.title.toLowerCase()) return 1;
          return 0;
        });
    });
  }

  static getAtomInWorkflow({ video }) {
    return pandaReqwest({
      url: `${WorkflowApi.workflowUrl}/api/atom/${video.id}`,
      crossOrigin: true,
      withCredentials: true
    }).then(response => response.data);
  }

  static _getTrackInWorkflowPayload({ video, status, section }) {
    const prodOffice = getProductionOffice();

    const scheduledLaunchDate = VideoUtils.getScheduledLaunch(video);
    const core = {
      contentType: 'media',
      editorId: video.id,
      title: video.title,
      priority: 0,
      scheduledLaunchDate,
      needsLegal: 'NA',
      section,
      status,
      prodOffice
    };
       
    return core;
  }

  static trackInWorkflow({ video, status, section }) {
    const payload = WorkflowApi._getTrackInWorkflowPayload({
      video,
      status,
      section
    });

    return pandaReqwest({
      method: 'POST',
      url: `${WorkflowApi.workflowUrl}/api/stubs`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }
}
