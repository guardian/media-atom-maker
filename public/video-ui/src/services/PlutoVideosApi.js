import {pandaReqwest} from './pandaReqwest';

export default {

  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api/pluto',
      method: 'get'
    });
  },

  sendToPluto: (atomId, projectId) => {
    return pandaReqwest({
      url: '/api/pluto/' + atomId + '/send',
      method: 'put',
      contentType: 'application/json',
      data: JSON.stringify({plutoId: projectId})
    });
  }
};
