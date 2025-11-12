import { apiRequest } from './apiRequest';

export type IconikItemType = 'commissions' | 'projects' | 'working-groups';

export type IconikWorkingGroup = {
  id: string;
  title: string;
};

export type IconikCommission = {
  id: string;
  title: string;
  workingGroupId: string;
};

export type IconikProject = {
  id: string;
  title: string;
  status: string;
  commissionId: string;
  workingGroupId: string;
};

export function getIconikWorkingGroups() {
  return apiRequest<IconikWorkingGroup[]>({
    url: '/api/iconik/working-groups'
  });
}

export function getIconikCommissionsForWorkingGroup({
  workingGroupId
}: {
  workingGroupId: string;
}) {
  return apiRequest<IconikCommission[]>({
    url: `/api/iconik/working-groups/${workingGroupId}/commissions`
  });
}

export function getIconikProjectsForCommission({
  commissionId
}: {
  commissionId: string;
}) {
  return apiRequest<IconikProject[]>({
    url: `/api/iconik/commissions/${commissionId}/projects`
  });
}

export function getIconikItemById(id: string, itemType: IconikItemType) {
  return apiRequest<IconikProject | IconikCommission>({
    url: `/api/iconik/${itemType}/${id}`
  });
}
