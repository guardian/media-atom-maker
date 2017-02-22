import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';

export default class ContentApi {
  static get proxyUrl () {
    return getStore().getState().config.capiProxyUrl;
  }

  static search(query) {
    const encodedQuery = encodeURIComponent(query);

    return pandaReqwest({
      url: `${ContentApi.proxyUrl}/atoms?types=media&q=${encodedQuery}&searchFields=data.title`,
      method: 'get'
    });
  }

  static getByPath(path) {
    return pandaReqwest({
      url: `${ContentApi.proxyUrl}/${path}?show-fields=all`,
      method: 'get'
    });
  }
}
