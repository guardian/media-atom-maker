import {getStore} from '../util/storeAccessor';
import { pandaReqwest } from './pandaReqwest';

export default class TargetingApi {
  static get targetingUrl() {
    return getStore().getState().config.targetingUrl;
  }

  static createTarget({id, title, expiryDate}) {
    const coreData = {
      title,
      tagPaths: [],
      url : `/atom/media/${id}`
    };

    const data = Object.assign({}, coreData, expiryDate ? {activeUntil: expiryDate} : {});

    const params = {
      url: `${TargetingApi.targetingUrl}/api/suggestions`,
      method: 'post',
      data,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params);
  }

  static updateTarget({ id, ...data }) {
    const params = {
      url: `${TargetingApi.targetingUrl}/api/suggestions/${id}`,
      method: 'put',
      data,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params);
  }

  static deleteTarget({id}) {
    const params = {
      method: 'delete',
      url: `${TargetingApi.targetingUrl}/api/suggestions/${id}`,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params);
  }

  static getTargets({id}) {
    const params = {
      url: `${TargetingApi.targetingUrl}/api/suggestions/search?url=/atom/media/${id}`,
      crossOrigin: true,
      withCredentials: true
    };

    return pandaReqwest(params);
  }
}
