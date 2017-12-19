import { pandaReqwest } from './pandaReqwest';

export default {
  fetchPlutoVideos: () => {
    return pandaReqwest({
      url: '/api2/atoms/pluto/missing'
    });
  }
};
