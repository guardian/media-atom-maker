import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';

export default {

  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api/pluto',
      method: 'get'
    });
  }
};
