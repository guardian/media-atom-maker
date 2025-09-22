import { apiRequest } from './apiRequest';
import { getStore } from '../util/storeAccessor';
import VideoUtils from '../util/video';
import moment, { Moment } from 'moment';
import { impossiblyDistantDate } from '../constants/dates';

export type FlatStub<Priority, Date> = Omit<Stub<Priority, Date>, "externalData"> & ExternalData<Date>

export type Stub<Priority, Date> = {
  contentType: string,
  editorId?: string,
  priority: Priority
  title: string,
  needsLegal: NeedsLegal,
  section: string,
  prodOffice: string,
  commissioningDesks?: string,
  note?: string,
  externalData: ExternalData<Date>,
}

type NeedsLegal = 'NA' | 'Complete' | 'Required'

type ExternalData<Date> = {
  status: Status,
  commentable?: boolean,
  lastModified?: Date,
  published?: boolean,
  timePublished?: Date,
  headline?: string,
  sensitive?: boolean,
  legallySensitive?: boolean,
  optimisedForWeb?: boolean,
  path?: string,
  scheduledLaunchDate?: Date,
  embargoedUntil?: Date,
  embargoedIndefinitely?: boolean,
}

export type Section = {
  name: string,
  selected: boolean,
  id: number,
}

export type Status = string

export type ExpandedStatus = {
  id: string,
  title: string
}

export type Priority = {
  name: string,
  value: number,
}


type WorkflowDetails = {
    video: {
      id?: string,
      title: string,
      contentChangeDetails: {
        published: {
          date: string
        },
        lastModified: {
          date: string
        }
      },
      commentsEnabled?: boolean,
      commissioningDesks: string[],
      sensitive?: boolean,
      legallySensitive?: boolean,
      optimisedForWeb?: boolean,
    },
    status: string,
    section: string,
    note: string,
    prodOffice: string,
    priority?: string
  }

type ContentUpdate = {
  stubId: number,
  stubRowsUpdated?: number,
  collaboratorRowsUpdated?: number,
}

type ApiResponse<T> = {
  status: string,
  statusCode: number,
  data: T,
}

export default class WorkflowApi {
  static get workflowUrl() {
    return getStore().getState().config.workflowUrl;
  }

  static workflowItemLink(video: {id: string}) {
    return `${WorkflowApi.workflowUrl}/dashboard?editorId=${video.id}`;
  }

  static _getResponseAsJson<A>(response: string | unknown): A {
    if (typeof response === 'string') {
      return JSON.parse(response);
    }
    throw new Error(`Error calling Workflow API – expected string response but got: ${response}`);
  }

  //clean up the workflow data so that the priority field number, which can be 0, is converted to a string
  static _cleanUpWorkflowData<Date>(workflowData: FlatStub<number, Date>): FlatStub<string, Date> {
    return { ...workflowData, priority: workflowData.priority.toString() };
  }

  static getSections(): Promise<Section[]> {
    // timeout in case the user is not logged into Workflow
    const params = {
      url: `${WorkflowApi.workflowUrl}/api/sections`,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest<string | unknown>(params, 500).then(response => {
      return WorkflowApi._getResponseAsJson<ApiResponse<Section[]>>(response)
        .data.map(section => Object.assign({}, section, {
        id: section.name,
        title: section.name,
        workflowId: section.id
      })
      ).sort((a, b) => {
        if (a.title.toLowerCase() < b.title.toLowerCase())
          return -1;
        if (a.title.toLowerCase() > b.title.toLowerCase())
          return 1;
        return 0;
      });
    });
  }

  static getStatuses(): Promise<ExpandedStatus[]> {
    const params = {
      url: `${WorkflowApi.workflowUrl}/api/statuses`,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest<string | unknown>(params, 500).then(response => {
      return WorkflowApi._getResponseAsJson<{data: Status[]}>(response).data
        .filter(status => status.toLowerCase() !== 'stub')
        .map(status => Object.assign({}, { id: status, title: status })
        );
    });
  }

  static getPriorities(): Promise<Priority[]> {
    const params = {
      url: `${WorkflowApi.workflowUrl}/api/priorities`,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest<string | unknown>(params, 500).then(response => {
      return WorkflowApi._getResponseAsJson<Priority[]>(response)
    });
  }


  static getAtomInWorkflow({ id }: {id: string}): Promise<FlatStub<string, string>> {
    return apiRequest<string | unknown>({
      url: `${WorkflowApi.workflowUrl}/api/atom/${id}`,
      crossOrigin: true,
      withCredentials: true
    }).then(response => WorkflowApi._getResponseAsJson<{data: FlatStub<number, string>}>(response).data)
      .then(jsonRes => WorkflowApi._cleanUpWorkflowData(jsonRes));
  }

  static _getTrackInWorkflowPayload(
    {
      video,
      status,
      section,
      note,
      prodOffice,
      priority
    }: WorkflowDetails
  ): FlatStub<string, Moment> {

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
      priority: priority,
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

  static trackInWorkflow({ video, status, section, note, prodOffice, priority }: WorkflowDetails): Promise<ApiResponse<ContentUpdate>> {
    const payload = WorkflowApi._getTrackInWorkflowPayload({
      video,
      status,
      section,
      note,
      prodOffice,
      priority
    });

    return apiRequest<ApiResponse<ContentUpdate>, FlatStub<string, Moment>>({
      method: 'POST',
      url: `${WorkflowApi.workflowUrl}/api/stubs`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updateProdOffice({ id, prodOffice }: {id: string, prodOffice: string}): Promise<ApiResponse<number>> {
    const payload = {
      data: prodOffice
    };

    return apiRequest<ApiResponse<number>, {data: string}>({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/prodOffice`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updateStatus({ id, status }: {id: string, status: string}): Promise<ApiResponse<number>> {
    const payload = {
      data: status
    };

    return apiRequest<ApiResponse<number>, {data: string}>({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/status`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updateNote({ id, note }: {id: string, note: string}): Promise<ApiResponse<number>> {
    if (!note) return; //property is optional so may be null

    const payload = {
      data: note
    };

    return apiRequest<ApiResponse<number>, {data: string}>({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/note`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }

  static updatePriority({ id, priority }: {id: string, priority: string}): Promise<ApiResponse<number>> {
    if (priority === null) return; //property is optional so may be null, but 0 is a valid value

    const payload = {
      data: priority
    };

    return apiRequest<ApiResponse<number>, {data: string}>({
      method: 'PUT',
      url: `${WorkflowApi.workflowUrl}/api/stubs/${id}/priority`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }
}
