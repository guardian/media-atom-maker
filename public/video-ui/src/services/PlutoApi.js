import { pandaReqwest } from './pandaReqwest';

export function getPlutoProjects() {
  return pandaReqwest({
    url: '/api2/pluto/projects'
  });
}
