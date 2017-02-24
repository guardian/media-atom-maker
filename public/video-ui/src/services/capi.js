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

  static getByPath(path, retry = false) {
    const retryTimeout = retry ? 10 * 1000 : 0; // retry up to 10 seconds

    return pandaReqwest({
      url: `${ContentApi.proxyUrl}/${path}?show-fields=all`,
      method: 'get'
    }, retryTimeout);
  }
}
