import {getStore} from '../util/storeAccessor';
import { pandaReqwest } from './pandaReqwest';

export default class TargetingApi {
  static get targetingUrl() {
    return getStore().getState().config.targetingUrl;
  }

  static get getEmail() {
    return getStore().getState().config.presence.email;
  }

  static createTarget({id, title, tags, expiryDate}) {
    const coreData = {
      title,
      tagPaths: tags,
      url : `/atom/media/${id}`,
      createdBy: TargetingApi.getEmail()
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
