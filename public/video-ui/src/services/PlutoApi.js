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

/**
 * return a link to the given pluto project as a string.
 * since we have changed the form of the pluto ID, we can't now detect which environment the item is from
 * based on the ID so this is a static string for the time being.  Needs some thought about how to support prod/dev in future.
 * @param projectId
 */
export function getPlutoProjectLink(projectId) {
  return `https://pluto.gnm.int/pluto-core/project/${projectId}`
}
