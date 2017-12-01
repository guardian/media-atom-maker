import { pandaReqwest } from './pandaReqwest';

export function getPlutoCommissions() {
  return pandaReqwest({
    url: '/api2/pluto/commissions'
  });
}

export function getPlutoProjects({commissionId}) {
  return pandaReqwest({
    url: `/api2/pluto/commissions/${commissionId}/projects`
  });
}
