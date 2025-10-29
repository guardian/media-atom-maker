import { apiRequest } from './apiRequest';

type PlutoItemType = 'commissions' | 'projects';

export type PlutoCommission = {
  id: string;
  title: string;
};

export type PlutoProject = {
  id: string;
  title: string;
  status: string;
  commissionId: string;
  commissionTitle: string; //TODO remove this once migrated
  productionOffice: string;
};

export function getPlutoCommissions() {
  return apiRequest<PlutoCommission[]>({
    url: '/api/pluto/commissions'
  });
}

export function getPlutoProjects({ commissionId }: { commissionId: string }) {
  return apiRequest<PlutoProject[]>({
    url: `/api/pluto/commissions/${commissionId}/projects`
  });
}

export function getPlutoItemById(id: string, itemType: PlutoItemType) {
  return apiRequest<PlutoProject | PlutoCommission>({
    url: `/api/pluto/${itemType}/${id}`
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
