import { pandaReqwest } from './pandaReqwest';

type PlutoItemType = 'commissions' | 'projects'

type PlutoCommission = {
  id: string,
  title: string
}

type PlutoProject = {
  id: string,
  title: string,
  status: string,
  commissionId: string,
  commissionTitle: string, //TODO remove this once migrated
  productionOffice: string
}

export function getPlutoCommissions() {
  return pandaReqwest<PlutoCommission[]>({
    url: '/api/pluto/commissions'
  });
}

export function getPlutoProjects({ commissionId }: { commissionId: string }) {
  return pandaReqwest<PlutoProject[]>({
    url: `/api/pluto/commissions/${commissionId}/projects`
  });
}

export function getPlutoItemById(id: string, itemType: PlutoItemType) {
  return pandaReqwest<PlutoProject | PlutoCommission>({
    url: `/api/pluto/${itemType}s/${id}`
  });
}

/**
 * return a link to the given pluto project as a string.
 * since we have changed the form of the pluto ID, we can't now detect which environment the item is from
 * based on the ID so this is a static string for the time being.  Needs some thought about how to support prod/dev in future.
 * @param projectId
 */
export function getPlutoProjectLink(projectId: string) {
  return `https://pluto.gnm.int/pluto-core/project/${projectId}`;
}
