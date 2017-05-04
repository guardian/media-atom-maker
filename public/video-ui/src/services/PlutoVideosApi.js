import {pandaReqwest} from './pandaReqwest';

export default {

  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api2/pluto',
      method: 'get'
    });
  },

  sendToPluto: (atomId, projectId) => {
    return pandaReqwest({
      url: '/api2/pluto/' + atomId,
      method: 'put',
      contentType: 'application/json',
      data: {plutoId: projectId}
    });
  }
};
