import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';

export function fetchUsages(videoId) {
  
  const capiProxyUrl = getStore().getState().config.capiProxyUrl;
  return pandaReqwest({
    url: capiProxyUrl + "/atom/media/" + videoId + "/usage",
    method: 'get',
  })
}
