export type UpsertMessage = {
  type: 'project-created' | 'project-updated';
  id: string;
  title: string;
  status: string;
  commissionId: string;
  commissionTitle: string;
  productionOffice: string;
  created: string;
};

export function isUpsertMessage(data: any): data is UpsertMessage {
  return (
    data &&
    (data.type === 'project-created' || data.type === 'project-updated') &&
    typeof data.id === 'string' &&
    typeof data.title === 'string' &&
    typeof data.status === 'string' &&
    typeof data.commissionId === 'string' &&
    typeof data.commissionTitle === 'string' &&
    typeof data.productionOffice === 'string' &&
    typeof data.created === 'string'
  );
}

export type DeleteMessage = {
  type: 'project-created' | 'project-updated';
  commissionId: string;
  commissionTitle: '(DELETE)';
};

export function isDeleteMessage(data: any): data is DeleteMessage {
  return (
    data &&
    (data.type === 'project-created' || data.type === 'project-updated') &&
    typeof data.commissionId === 'string' &&
    data.commissionTitle === '(DELETE)'
  );
}
