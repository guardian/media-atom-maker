import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';

export default class ContentApi {
  static get proxyUrl() {
    return getStore().getState().config.capiProxyUrl;
  }

  static get liveProxyUrl() {
    return getStore().getState().config.liveCapiProxyUrl;
  }

  static search(query) {
    const encodedQuery = encodeURIComponent(query);

    return pandaReqwest({
      url: `${ContentApi.proxyUrl}/atoms?types=media&q=${encodedQuery}&searchFields=data.title`
    });
  }

  static getByPath(path, retry = false) {
    const retryTimeout = retry ? 10 * 1000 : 0; // retry up to 10 seconds

    return pandaReqwest(
      {
        url: `${ContentApi.proxyUrl}/${path}?show-fields=all`
      },
      retryTimeout
    );
  }

  static getLivePage(id) {
    return pandaReqwest({
      url: `${ContentApi.liveProxyUrl}/${id}`
    });
  }
}
