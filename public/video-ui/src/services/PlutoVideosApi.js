import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';

export default {

  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api/pluto',
      method: 'get'
    });
  },

  addPlutoProject: (atomId, projectId) => {
    return pandaReqwest({
      url: '/api/pluto/' + atomId + '/add/',
      method: 'post',
      data: JSON.stringify({plutoId: projectId})
    });
  }
};
