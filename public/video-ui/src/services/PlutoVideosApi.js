import { pandaReqwest } from './pandaReqwest';

export default {
  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api2/atoms/pluto/missing'
    });
  },

  sendToPluto: (atomId, projectId) => {
    return pandaReqwest({
      url: `/api2/atoms/${atomId}/pluto`,
      method: 'put',
      data: { plutoId: projectId }
    });
  }
};
