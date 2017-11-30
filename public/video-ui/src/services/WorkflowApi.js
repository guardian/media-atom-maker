import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import getProductionOffice from '../util/getProductionOffice';
// import VideoUtils from '../util/video';
import moment from 'moment';

export default class WorkflowApi {
  static get workflowUrl() {
    return getStore().getState().config.workflowUrl;
  }

  static workflowItemLink(video) {
    return `${WorkflowApi.workflowUrl}/dashboard?editorId=${video.id}`;
  }

  static _getResponseAsJson(response) {
    if (typeof response === 'string') {
      return JSON.parse(response);
    }
    return response;
  }

  static getSections() {
    // timeout in case the user is not logged into Workflow
    const params = {
      url: `${WorkflowApi.workflowUrl}/api/sections`,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params, 500).then(response => {
      return WorkflowApi._getResponseAsJson(response)
        .data.map(section =>
          Object.assign({}, section, { title: section.name })
        )
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
    }).then(response => WorkflowApi._getResponseAsJson(response).data);
  }

  static _getTrackInWorkflowPayload({
    video,
    status,
    section,
    scheduledLaunchDate
  }) {
    const prodOffice = getProductionOffice();

    const { contentChangeDetails } = video;

    const publishedDate = contentChangeDetails.published
      ? moment(contentChangeDetails.published.date)
      : null;

    const lastModifiedDate = contentChangeDetails.lastModified
      ? moment(contentChangeDetails.lastModified.date)
      : null;

    const core = {
      contentType: 'media',
      editorId: video.id,
      title: video.title,
      priority: 0,
      needsLegal: 'NA',
      section,
      status,
      prodOffice,
      commentable: video.commentsEnabled,
      commissioningDesks: video.commissioningDesks.join(),
      lastModified: lastModifiedDate,
      published: !!contentChangeDetails.published,
      timePublished: publishedDate,
      headline: video.title,
      sensitive: video.sensitive,
      legallySensitive: video.legallySensitive,
      optimisedForWeb: video.optimisedForWeb,
      path: 'atom/media/' + video.id
    };

    if (!scheduledLaunchDate) {
      return core;
    }

    const momentLaunchDate = moment(scheduledLaunchDate);

    return Object.assign({}, core, {
      scheduledLaunchDate: momentLaunchDate,
      note: `Please create a Video page, launching ${momentLaunchDate.format('DD MMM YYYY HH:mm')}`
    });
  }

  static trackInWorkflow({ video, status, section, scheduledLaunchDate }) {
    const payload = WorkflowApi._getTrackInWorkflowPayload({
      video,
      status,
      section,
      scheduledLaunchDate
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
