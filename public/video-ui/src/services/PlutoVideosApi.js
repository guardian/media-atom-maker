import {pandaReqwest} from './pandaReqwest';

export default {

  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api2/pluto-list',
      method: 'get'
    });
  },

  sendToPluto: (atomId, projectId) => {
    return pandaReqwest({
      url: '/api2/pluto/' + atomId + '/send',
      method: 'put',
      contentType: 'application/json',
      data: JSON.stringify({plutoId: projectId})
    });
  }
};
