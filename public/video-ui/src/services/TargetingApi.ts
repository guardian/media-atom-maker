import { getStore } from '../util/storeAccessor';
import { apiRequest } from './apiRequest';
import moment from 'moment';

const getFortnight = () => moment().add('days', 14).valueOf();

export default class TargetingApi {
  static get targetingUrl() {
    return getStore().getState().config.targetingUrl;
  }

  static createTarget({ id, title, expiryDate }: any) {
    const data = {
      title,
      // @ts-expect-error TS(7018): Object literal's property 'tagPaths' implicitly ha... Remove this comment to see the full error message
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

  static deleteTarget({ id }: any) {
    const params = {
      method: 'delete',
      url: `${TargetingApi.targetingUrl}/api/suggestions/${id}`,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest(params);
  }

  static getTargets({ id }: any) {
    const params = {
      url: `${TargetingApi.targetingUrl}/api/suggestions/search?url=/atom/media/${id}`,
      crossOrigin: true,
      withCredentials: true
    };

    return apiRequest(params);
  }
}
