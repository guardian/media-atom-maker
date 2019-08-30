import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import VideoUtils from '../util/video';
import moment from 'moment';
import { impossiblyDistantDate } from '../constants/dates';

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
          Object.assign({}, section, {
            id: section.name,
            title: section.name,
            workflowId: section.id
          })
        )
        .sort((a, b) => {
          if (a.title.toLowerCase() < b.title.toLowerCase()) return -1;
          if (a.title.toLowerCase() > b.title.toLowerCase()) return 1;
          return 0;
        });
    });
  }

  static getStatuses() {
    const params = {
      url: `${WorkflowApi.workflowUrl}/api/statuses`,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params, 500).then(response => {
      return WorkflowApi._getResponseAsJson(response).data
        .filter(status => status.toLowerCase() !== 'stub')
        .map(status =>
          Object.assign({}, { id: status, title: status })
        );
    });
  }

  static getPriorities() {
    const params = {
      url: `${WorkflowApi.workflowUrl}/api/priorities`,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params, 500).then(response => {
      return WorkflowApi._getResponseAsJson(response);
    });
  }

  static getAtomInWorkflow({ id }) {
    return pandaReqwest({
      url: `${WorkflowApi.workflowUrl}/api/atom/${id}`,
      crossOrigin: true,
      withCredentials: true
    }).then(response => WorkflowApi._getResponseAsJson(response).data);
  }

  static _getTrackInWorkflowPayload({
    video,
    status,
    section,
    note,
    prodOffice
  }) {

    const { contentChangeDetails } = video;

    const publishedDate = contentChangeDetails.published
      ? moment(contentChangeDetails.published.date)
      : null;

    const lastModifiedDate = contentChangeDetails.lastModified
      ? moment(contentChangeDetails.lastModified.date)
      : null;

    const scheduledLaunch = VideoUtils.getScheduledLaunchAsDate(video);
    const embargoDate = VideoUtils.getEmbargoAsDate(video);

    const [embargo, indefiniteEmbargo] =
      (embargoDate && embargoDate >= impossiblyDistantDate) ? [null, true] : [embargoDate, false];

    return {
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
      path: 'atom/media/' + video.id,
      scheduledLaunchDate: scheduledLaunch,
      embargoedUntil: embargo,
      embargoedIndefinitely: indefiniteEmbargo,
      note
    };
  }

  static trackInWorkflow({ video, status, section, note, prodOffice }) {
    const payload = WorkflowApi._getTrackInWorkflowPayload({
      video,
      status,
      section,
      note,
      prodOffice
    });

    return pandaReqwest({
      method: 'POST',
      url: `${WorkflowApi.workflowUrl}/api/stubs`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updateProdOffice({ id, prodOffice }) {
    const payload = {
      data: prodOffice
    };

    return pandaReqwest({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/prodOffice`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updateStatus({ id, status }) {
    const payload = {
      data: status
    };

    return pandaReqwest({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/status`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updateNote({ id, note }) {
    const payload = {
      data: note
    };

    return pandaReqwest({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/note`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updatePriority({ id, priority }) {
    const payload = {
      data: priority
    };

    return pandaReqwest({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/priority`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }
}
