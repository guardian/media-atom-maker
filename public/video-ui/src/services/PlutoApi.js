import { pandaReqwest } from './pandaReqwest';

export function getPlutoCommissions() {
  return pandaReqwest({
    url: '/api/pluto/commissions'
  });
}

export function getPlutoProjects({commissionId}) {
  return pandaReqwest({
    url: `/api/pluto/commissions/${commissionId}/projects`
  });
}
