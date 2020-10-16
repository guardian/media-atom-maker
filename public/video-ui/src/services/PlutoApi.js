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

export function getPlutoItemById(id, itemType) {
  return pandaReqwest({
    url: `/api/pluto/${itemType}s/${id}`
  });
}

export function getPlutoProjectLink(projectId) {
  // `plutoSources` lifted from flexible-content
  // https://github.com/guardian/flexible-content/blob/master/composer/src/js/controllers/content/video/body-block.js
  const plutoDev = { prefix: 'VX-', domain: 'pluto-dev' };

  const plutoSources = [
    { prefix: 'KP-', domain: 'pluto' },
    { prefix: 'BK-', domain: 'pluto-dr' },
    plutoDev
  ];

  const plutoSource = plutoSources.find(source => projectId.startsWith(source.prefix)) || plutoDev;

  // pluto isn't accessible over https
  return `http://${plutoSource.domain}/project/${projectId}`;
}
