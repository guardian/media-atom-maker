import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';

export default {

  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api/pluto',
      method: 'get'
    });
  },

  addPlutoProjectManual: (atomId, projectId) => {
    return pandaReqwest({
      url: '/api/pluto/' + atomId + '/add-manual/',
      method: 'post',
      data: JSON.stringify({plutoId: projectId})
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
