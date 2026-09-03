import { getStore } from '../util/storeAccessor';
import { apiRequest } from './apiRequest';
import moment from 'moment';

const getFortnight = () => moment().add('days', 14).valueOf();

export default class TargetingApi {
  static get targetingUrl() {
    return getStore().getState().config.targetingUrl;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  static createTarget({ id, title, expiryDate }: any) {
    const data = {
      title,
      tagPaths: [],
      url: `/atom/media/${id}`,
      activeUntil: expiryDate || getFortnight()
    };

    const params = {
      url: `${TargetingApi.targetingUrl}/api/suggestions`,
      method: 'post',
      data,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest(params);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  static updateTarget({ id, ...data }: any) {
    const params = {
      url: `${TargetingApi.targetingUrl}/api/suggestions/${id}`,
      method: 'put',
      data,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest(params);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  static deleteTarget({ id }: any) {
    const params = {
      method: 'delete',
      url: `${TargetingApi.targetingUrl}/api/suggestions/${id}`,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest(params);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  static getTargets({ id }: any) {
    const params = {
      url: `${TargetingApi.targetingUrl}/api/suggestions/search?url=/atom/media/${id}`,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest(params);
  }
}
