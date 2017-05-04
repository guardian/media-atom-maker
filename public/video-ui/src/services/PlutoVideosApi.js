import {pandaReqwest} from './pandaReqwest';

export default {

  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api2/pluto'
    });
  },

  sendToPluto: (atomId, projectId) => {
    return pandaReqwest({
      url: '/api2/pluto/' + atomId,
      method: 'put',
      data: {plutoId: projectId}
    });
  }
};
