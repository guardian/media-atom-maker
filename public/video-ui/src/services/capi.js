import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';

export function fetchComposerId(capiId) {
  const capiProxyUrl = getStore().getState().config.capiProxyUrl;
  const url = capiProxyUrl + '/' + capiId + "?show-fields=all";
  return pandaReqwest({
    url: capiProxyUrl + '/' + capiId + "?show-fields=all",
    method: 'get'
  })
  .then(resp => {
    if (resp.response.content && resp.response.content.fields&& resp.response.content.fields.internalComposerCode) {
      return resp.response.content.fields.internalComposerCode;
    }
    return "";

  });
}

export function searchText(query) {
  const capiProxyUrl = getStore().getState().config.capiProxyUrl;
  return pandaReqwest({
    url: capiProxyUrl + "/atoms?types=media&q=" + query + "&searchFields=data.title",
    method: 'get'
  })
}
