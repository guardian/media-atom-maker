import { apiRequest } from './apiRequest';
import { getStore } from '../util/storeAccessor';
import debounce from "lodash/debounce";

export type Stage = 'published' | 'preview'

export type CapiResponse<TContent> = {
  content: TContent | undefined;
};

export type CapiContentResponse = CapiResponse<CapiContent>;

export type CapiContent = {
  id: string;
  type: string;
  fields: {
    creationDate: string;
    internalComposerCode: string;
  };
};

type ApiReponse<T> = {
  response: T
}

export default class ContentApi {
  static get published(): Stage {
    return 'published';
  }

  static get preview(): Stage {
    return 'preview';
  }

  static getUrl(stage: Stage) {
    return stage === ContentApi.published
      ? ContentApi.liveProxyUrl
      : ContentApi.proxyUrl;
  }

  static get proxyUrl() {
    return getStore().getState().config.capiProxyUrl;
  }

  static get liveProxyUrl() {
    return getStore().getState().config.liveCapiProxyUrl;
  }

  static get tagManagerUrl() {
    return getStore().getState().config.tagManagerUrl;
  }

  static search(query: string) {
    const encodedQuery = encodeURIComponent(query);

    return apiRequest({
      url: `${ContentApi.proxyUrl}/atoms?types=media&q=${encodedQuery}&searchFields=data.title`
    });
  }

  static getByPath(path: string, retry = false): Promise<ApiReponse<CapiContentResponse>> {
    const retryTimeout = retry ? 10 * 1000 : 0; // retry up to 10 seconds

    return apiRequest(
      {
        url: `${ContentApi.proxyUrl}/${path}?show-fields=all`
      },
      retryTimeout
    );
  }

  static getLivePage(id: string) {
    return apiRequest({
      url: `${ContentApi.liveProxyUrl}/${id}`
    });
  }

  static getTagsByType(query: string, types: string[]) {
    return apiRequest({
      url: `${ContentApi.tagManagerUrl}/hyper/tags?query=${query}&limit=150&type=${types.join(",")}`
    });
  }
}
